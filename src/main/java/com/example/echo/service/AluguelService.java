package com.example.echo.service;

import com.example.echo.dto.AluguelDTO;
import com.example.echo.dto.BicicletaDTO;
import com.example.echo.dto.NovoAluguelDTO;
import com.example.echo.dto.DevolucaoDTO;
import com.example.echo.dto.externo.CobrancaDTO;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import com.example.echo.repository.AluguelRepository;
import com.example.echo.repository.CiclistaRepository;
import com.example.echo.service.externo.ExternoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AluguelService {

    @Autowired
    private AluguelRepository aluguelRepository;
    @Autowired
    private CiclistaRepository ciclistaRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //serviços externos
    @Autowired
    private ExternoClient externoClient;

    @Autowired
    private EquipamentoService equipamentoService;

    private static final Double CUSTO_INICIAL = 10.00;

    public AluguelDTO realizarAluguel(NovoAluguelDTO dto) {
        //valida cicista
        Ciclista ciclista = ciclistaRepository.findById(dto.getCiclistaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclista não encontrado"));

        if (ciclista.getStatus() != StatusCiclista.ATIVO) {
            throw new DadosInvalidosException("Cadastro do ciclista não está ativo.");
        }

        //E1
        if (aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(dto.getCiclistaId())) {

            emailService.enviarEmail(ciclista.getEmail(), "Tentativa de Aluguel", "Você já possui uma bicicleta alugada em andamento.");

            throw new DadosInvalidosException("Ciclista já possui um aluguel em andamento.");
        }

        //checa bike
        BicicletaDTO bicicleta = equipamentoService.buscarBicicletaNaTranca(dto.getTrancaInicioId());

        //E2
        if (bicicleta == null) {
            throw new DadosInvalidosException("Não existe bicicleta na tranca informada.");
        }
        //E4
        if ("EM_REPARO".equalsIgnoreCase(bicicleta.getStatus())) {
            throw new DadosInvalidosException("A bicicleta escolhida está em reparo.");
        }

        CobrancaDTO cobrancaConfirmada = null;
        try {
            cobrancaConfirmada = externoClient.realizarCobranca(CUSTO_INICIAL, dto.getCiclistaId());
        } catch (Exception e) {
            // Se a API externa cair ou recusar, lançamos erro e interrompemos o aluguel
            throw new DadosInvalidosException("Falha ao processar pagamento inicial: " + e.getMessage());
        }

        //destranca
        boolean destrancou = equipamentoService.destrancarTranca(dto.getTrancaInicioId());
        if (!destrancou) {
            //E5
            throw new DadosInvalidosException("Erro ao destrancar a bicicleta. Tente novamente.");
        }

        //salva aluguel
        Aluguel aluguel = new Aluguel();
        aluguel.setCiclista(ciclista);
        aluguel.setBicicleta(bicicleta.getId());
        aluguel.setTrancaInicio(dto.getTrancaInicioId());
        aluguel.setHoraInicio(LocalDateTime.now());
        aluguel.setCobranca(cobrancaConfirmada.getId());

        Aluguel salvo = aluguelRepository.save(aluguel);

        //email sucesso
        emailService.enviarEmail(ciclista.getEmail(), "Aluguel Iniciado", "Bom passeio!");

        return new AluguelDTO(salvo);
    }

    public AluguelDTO realizarDevolucao(DevolucaoDTO dto) {
        //validar e buscar aluguel ativo , sem fim
        Aluguel aluguel = aluguelRepository.findByCiclistaIdAndHoraFimIsNull(dto.getCiclistaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não há aluguel em andamento para este ciclista."));

        //Definir fim
        aluguel.setHoraFim(LocalDateTime.now());
        aluguel.setTrancaFim(dto.getTrancaFimId());

        //calcula valor
        //2 horas preço fixo + 5 reais por meia hora
        long minutosTotais = ChronoUnit.MINUTES.between(aluguel.getHoraInicio(), aluguel.getHoraFim());
        double custoExtra = 0.0;

        if (minutosTotais > 120) { // [A1] Excedeu 2 horas
            long minutosExcedentes = minutosTotais - 120;
            //arredonda pra cima
            double blocosMeiaHora = Math.ceil(minutosExcedentes / 30.0);
            custoExtra = blocosMeiaHora * 5.00;
        }

        aluguel.setValorExtra(custoExtra);

        //cobra o extra
        if (custoExtra > 0) {
            try {
                // TENTATIVA 1: Cobrança Direta
                CobrancaDTO cobrancaRealizada = externoClient.realizarCobranca(custoExtra, dto.getCiclistaId());
                aluguel.setCobranca(cobrancaRealizada.getId());

            } catch (Exception e) {
                System.err.println("Falha na cobrança direta: " + e.getMessage());
                System.out.println("Tentando enviar para a fila de cobranças...");

                try {
                    // TENTATIVA 2: Enviar para a Fila (Fallback)
                    CobrancaDTO cobrancaFila = new CobrancaDTO();
                    cobrancaFila.setValor(custoExtra);
                    cobrancaFila.setCiclista(dto.getCiclistaId());

                    externoClient.adicionarCobrancaNaFila(cobrancaFila);

                    // Nota: Na fila, talvez não tenhamos o ID imediato, então o campo 'cobranca'
                    // no aluguel pode ficar nulo ou você pode criar um log.

                } catch (Exception exFila) {
                    // ÚLTIMO CASO: Tudo falhou.
                    // Apenas logamos o erro Crítico, mas deixamos o código seguir para devolver a bike.
                    // Na vida real, salvaríamos isso numa tabela de "Pendências" local.
                    System.err.println("CRÍTICO: Falha total na cobrança (Direta e Fila). O valor extra não foi cobrado.");
                }
            }
        }

        //atualiza bike
        String novoStatusBike = "DISPONIVEL";

        if (dto.isDefeito()) { //pediu reparo
            novoStatusBike = "EM_REPARO";
        }

        //Chama serviço externo de equipamento falso
        equipamentoService.alterarStatusBicicleta(aluguel.getBicicleta(), novoStatusBike);

        equipamentoService.trancarTranca(dto.getTrancaFimId(), aluguel.getBicicleta());

        //salva
        Aluguel salvo = aluguelRepository.save(aluguel);

        //notificação
        String mensagem = String.format(
                "Devolução realizada na tranca %d.\nTempo total: %d min.\nCusto extra: R$ %.2f\nStatus Bike: %s",
                salvo.getTrancaFim(),
                minutosTotais,
                custoExtra,
                novoStatusBike
        );
        emailService.enviarEmail(aluguel.getCiclista().getEmail(), "Bicicleta Devolvida", mensagem);

        return new AluguelDTO(salvo);
    }

    //
    @Transactional
    public void restaurarDados() {
        jdbcTemplate.execute("TRUNCATE TABLE tb_aluguel RESTART IDENTITY CASCADE");

        // 2. Depois apaga a tabela principal (Ciclista)
        jdbcTemplate.execute("TRUNCATE TABLE tb_ciclista RESTART IDENTITY CASCADE");

        // (Se tiver tabela de CartaoDeCredito separada, limpe ela aqui também)

        // ---------------------------------------------------------
        // 2. RECRIAÇÃO (O Maestro chamando os atores)
        // ---------------------------------------------------------

        // PRIMEIRO cria os ciclistas (para gerar os IDs 1, 2, 3...)
        criarCiclistasPadrao();

        // DEPOIS cria o aluguel (pois ele precisa do Ciclista 3 que acabou de ser criado)
        criarAluguelPadrao();
    }

    private void criarCiclistasPadrao() {
        // --- CICLISTA 1 (ATIVO) ---
        Ciclista c1 = new Ciclista();
        c1.setStatus(StatusCiclista.ATIVO);
        c1.setNome("Fulano Beltrano");
        c1.setNascimento(LocalDate.of(2021, 5, 2));
        c1.setCpf("78804034009");
        c1.setNacionalidade(Nacionalidade.BRASILEIRO);
        c1.setEmail("user@example.com");
        c1.setSenha("ABC123");
        c1.setUrlFotoDocumento("http://foto.com/doc.png");

        CartaoDeCredito cartao1 = new CartaoDeCredito();
        cartao1.setNomeTitular("Fulano Beltrano");
        cartao1.setNumero("4012001037141112");
        cartao1.setValidade(LocalDate.of(2022, 12, 1));
        cartao1.setCvv("132");
        c1.setCartaoDeCredito(cartao1);

        ciclistaRepository.save(c1);

        // --- CICLISTA 2 (AGUARDANDO CONFIRMAÇÃO) ---
        Ciclista c2 = new Ciclista();
        c2.setStatus(StatusCiclista.AGUARDANDO_CONFIRMACAO);
        c2.setNome("Ciclano de Tal");
        c2.setNascimento(LocalDate.of(2000, 1, 1));
        c2.setCpf("43943488039");
        c2.setNacionalidade(Nacionalidade.BRASILEIRO);
        c2.setEmail("user2@example.com");
        c2.setSenha("senha123");

        CartaoDeCredito cartao2 = new CartaoDeCredito();
        cartao2.setNomeTitular("Ciclano");
        cartao2.setNumero("1234567812345678");
        cartao2.setValidade(LocalDate.now().plusYears(1));
        cartao2.setCvv("111");
        c2.setCartaoDeCredito(cartao2);

        ciclistaRepository.save(c2);

        // --- CICLISTA 3 (ESTRANGEIRO) ---
        Ciclista c3 = new Ciclista();
        c3.setStatus(StatusCiclista.ATIVO);
        c3.setNome("John Doe");
        c3.setNascimento(LocalDate.of(1990, 2, 2));
        c3.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        c3.setEmail("user3@example.com");
        c3.setSenha("senha123");
        c3.setPassaporteNumero("P123456");
        c3.setPassaportePais("US");
        c3.setPassaporteValidade(LocalDate.of(2025, 1, 1));

        CartaoDeCredito cartao3 = new CartaoDeCredito();
        cartao3.setNomeTitular("John Doe");
        cartao3.setNumero("4444555566667777");
        cartao3.setValidade(LocalDate.now().plusYears(2));
        cartao3.setCvv("007");
        c3.setCartaoDeCredito(cartao3);

        ciclistaRepository.save(c3);
    }

    private void criarAluguelPadrao() {
        Ciclista ciclista3 = ciclistaRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("Erro ao restaurar: Ciclista 3 não criado"));

        Aluguel aluguel = new Aluguel();

        // AQUI ESTÁ A CORREÇÃO:
        // Em vez de setCiclistaId(3L), usamos setCiclista(objeto)
        aluguel.setCiclista(ciclista3);

        // Bicicleta e Tranca geralmente são apenas IDs (Long) se forem de outro microsserviço
        // Se a sua classe Aluguel pede Objeto Bicicleta, a lógica seria a mesma do Ciclista.
        // Mas baseado no seu erro, parece que só o Ciclista é entidade local.
        aluguel.setBicicleta(3L);
        aluguel.setTrancaInicio(2L);
        aluguel.setHoraInicio(LocalDateTime.now());

        // Ajuste o status conforme seu Enum (ATIVO, EM_ANDAMENTO, etc)
        // aluguel.setStatus(StatusAluguel.EM_ANDAMENTO);

        aluguelRepository.save(aluguel);
    }
}