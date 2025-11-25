package com.example.echo.service;

import com.example.echo.dto.AluguelDTO;
import com.example.echo.dto.BicicletaDTO;
import com.example.echo.dto.NovoAluguelDTO;
import com.example.echo.dto.DevolucaoDTO;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.model.Aluguel;
import com.example.echo.model.Ciclista;
import com.example.echo.model.StatusCiclista;
import com.example.echo.repository.AluguelRepository;
import com.example.echo.repository.CiclistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    //serviços falsos
    @Autowired
    private EquipamentoService equipamentoService;
    @Autowired
    private CobrancaService cobrancaService;

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

        //cobrança
        boolean pago = cobrancaService.autorizarPagamento(ciclista.getCartaoDeCredito().getNumero(), CUSTO_INICIAL);

        Long idCobranca; //vai vir de serviço externo
        if (!pago) {
            //E3
            cobrancaService.registrarCobrancaPendente(ciclista.getId(), CUSTO_INICIAL);
            throw new DadosInvalidosException("Pagamento não autorizado.");
        } else {
            idCobranca = System.currentTimeMillis(); //id falso
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
        aluguel.setCobranca(idCobranca);
        aluguel.setHoraInicio(LocalDateTime.now());

        Aluguel salvo = aluguelRepository.save(aluguel);

        //email sucesso
        emailService.enviarEmail(ciclista.getEmail(), "Aluguel Iniciado", "Bom passeio!");

        return new AluguelDTO(salvo);
    }

    public AluguelDTO realizarDevolucao(DevolucaoDTO dto) {
        // 1. Validar e Buscar Aluguel Ativo
        Aluguel aluguel = aluguelRepository.findByCiclistaIdAndHoraFimIsNull(dto.getCiclistaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não há aluguel em andamento para este ciclista."));

        // 2. Definir dados de término
        aluguel.setHoraFim(LocalDateTime.now());
        aluguel.setTrancaFim(dto.getTrancaFimId());

        // 3. Calcular Valor a Pagar [Fluxo Principal 3] [R1] [A1]
        // Regra: 2 horas (120 min) grátis (já pagas). Extra: R$ 5,00 por fração de 30min.
        long minutosTotais = ChronoUnit.MINUTES.between(aluguel.getHoraInicio(), aluguel.getHoraFim());
        double custoExtra = 0.0;

        if (minutosTotais > 120) { // [A1] Excedeu 2 horas
            long minutosExcedentes = minutosTotais - 120;
            // Math.ceil arredonda para cima (ex: 1 min extra = 1 bloco de 30 = R$ 5,00)
            double blocosMeiaHora = Math.ceil(minutosExcedentes / 30.0);
            custoExtra = blocosMeiaHora * 5.00;
        }

        aluguel.setValorExtra(custoExtra);

        // 4. Cobrança Extra [A1.1] [A1.2]
        if (custoExtra > 0) {
            boolean pagamentoAprovado = cobrancaService.autorizarPagamento(
                    aluguel.getCiclista().getCartaoDeCredito().getNumero(),
                    custoExtra
            );

            if (!pagamentoAprovado) {
                // [A2] Erro no pagamento -> Registra pendência
                cobrancaService.registrarCobrancaPendente(aluguel.getCiclista().getId(), custoExtra);
            }
            // Se aprovado, segue fluxo normal [A1.3]
        }

        // 5. Atualizar Status da Bicicleta [Fluxo Principal 5] ou [A3]
        String novoStatusBike = "DISPONIVEL";

        if (dto.isDefeito()) { // [A3] Ator requisitou reparo
            novoStatusBike = "EM_REPARO"; // ou "REPARO_SOLICITADO"
            // [A3.1] O sistema registra requisição (aqui simulado apenas pela mudança de status)
        }

        // Chama serviço externo de equipamento
        equipamentoService.alterarStatusBicicleta(aluguel.getBicicleta(), novoStatusBike);

        // 6. Fechar a Tranca [Fluxo Principal 6]
        equipamentoService.trancarTranca(dto.getTrancaFimId());

        // 7. Salvar Devolução [Fluxo Principal 4]
        Aluguel salvo = aluguelRepository.save(aluguel);

        // 8. Enviar Email [Fluxo Principal 7] [R3]
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

    // 1. Lógica para /ciclista/{id}/permiteAluguel
    public boolean permiteAluguel(Long ciclistaId) {
        // Verifica se ciclista existe (Swagger pede 404)
        Ciclista ciclista = ciclistaRepository.findById(ciclistaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclista não encontrado"));

        // Regra: Só pode alugar se estiver ATIVO
        if (ciclista.getStatus() != StatusCiclista.ATIVO) {
            return false;
        }

        // Regra: Só pode alugar se NÃO tiver aluguel em aberto
        boolean temAluguelAtivo = aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(ciclistaId);

        return !temAluguelAtivo; // Retorna TRUE se não tiver aluguel (pode alugar)
    }

    // 2. Lógica para /ciclista/{id}/bicicletaAlugada
    public BicicletaDTO buscarBicicletaAlugada(Long ciclistaId) {
        // Verifica se ciclista existe (Swagger pede 404)
        if (!ciclistaRepository.existsById(ciclistaId)) {
            throw new RecursoNaoEncontradoException("Ciclista não encontrado");
        }

        // Busca aluguel ativo
        Optional<Aluguel> aluguelOpt = aluguelRepository.findByCiclistaIdAndHoraFimIsNull(ciclistaId);

        if (aluguelOpt.isPresent()) {
            // Se tem aluguel, retorna a bicicleta correspondente
            Long idBike = aluguelOpt.get().getBicicleta();
            // Retornamos um DTO simples com o ID e status "EM_USO" (pois está alugada)
            return new BicicletaDTO(idBike, "EM_USO");
        }

        // Se não tem aluguel, retorna null (o controller vai tratar isso como body vazio)
        return null;
    }

    // 3. Lógica para /restaurarBanco (Reset total)
    public void restaurarBanco() {
        // A ordem importa por causa das chaves estrangeiras!
        // Primeiro apaga os alugueis (filhos)
        aluguelRepository.deleteAll();

        // Depois apaga os ciclistas (pais)
        ciclistaRepository.deleteAll();
    }
}