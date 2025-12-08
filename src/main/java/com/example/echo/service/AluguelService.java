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
            boolean pagamentoAprovado = cobrancaService.autorizarPagamento(
                    aluguel.getCiclista().getCartaoDeCredito().getNumero(),
                    custoExtra
            );

            if (!pagamentoAprovado) {
                //cobrança pendente
                cobrancaService.registrarCobrancaPendente(aluguel.getCiclista().getId(), custoExtra);
            }
        }

        //atualiza bike
        String novoStatusBike = "DISPONIVEL";

        if (dto.isDefeito()) { //pediu reparo
            novoStatusBike = "EM_REPARO";
        }

        //Chama serviço externo de equipamento falso
        equipamentoService.alterarStatusBicicleta(aluguel.getBicicleta(), novoStatusBike);

        equipamentoService.trancarTranca(dto.getTrancaFimId());

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
    public void restaurarBanco() {
        aluguelRepository.deleteAll();

        ciclistaRepository.deleteAll();
    }
}