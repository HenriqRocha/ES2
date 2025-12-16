package com.example.echo.service;

import com.example.echo.dto.*;
import com.example.echo.dto.externo.CobrancaDTO;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.model.Aluguel;
import com.example.echo.model.CartaoDeCredito;
import com.example.echo.model.Ciclista;
import com.example.echo.model.StatusCiclista;
import com.example.echo.repository.AluguelRepository;
import com.example.echo.repository.CiclistaRepository;
import com.example.echo.service.EmailService;
import com.example.echo.service.externo.EquipamentoClient;
import com.example.echo.service.externo.ExternoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AluguelServiceTest {

    @Mock private AluguelRepository aluguelRepository;
    @Mock private CiclistaRepository ciclistaRepository;
    @Mock private EmailService emailService;
    @Mock private EquipamentoService equipamentoService;

    @Mock private ExternoClient externoClient;

    @InjectMocks
    private AluguelService service;

    @InjectMocks
    private CiclistaService ciclistaService;

    private Ciclista ciclistaAtivo;
    private NovoAluguelDTO novoAluguelDTO;
    private BicicletaDTO bicicletaDisponivel;
    private DevolucaoDTO devolucaoDTO;
    private Aluguel aluguelAtivo;

    @BeforeEach
    void setUp() {
        // Ciclista válido com cartão
        ciclistaAtivo = new Ciclista();
        ciclistaAtivo.setId(1L);
        ciclistaAtivo.setStatus(StatusCiclista.ATIVO);
        ciclistaAtivo.setEmail("teste@email.com");

        CartaoDeCredito cartao = new CartaoDeCredito();
        cartao.setNumero("12345678");
        ciclistaAtivo.setCartaoDeCredito(cartao);

        // DTO de entrada padrão
        novoAluguelDTO = new NovoAluguelDTO();
        novoAluguelDTO.setCiclistaId(1L);
        novoAluguelDTO.setTrancaInicioId(10L);

        // Bicicleta mockada padrão
        bicicletaDisponivel = new BicicletaDTO(100L, "DISPONIVEL");
    }

    @Test
    @DisplayName("Deve realizar aluguel com sucesso (Caminho Feliz)")
    void deveRealizarAluguelComSucesso() {
        // Mocks de Sucesso
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(false);

        when(equipamentoService.buscarBicicletaNaTranca(10L)).thenReturn(bicicletaDisponivel);

        CobrancaDTO cobrancaSucesso = new CobrancaDTO();
        cobrancaSucesso.setId(123L);
        cobrancaSucesso.setStatus("OCUPADA");
        when(externoClient.realizarCobranca(anyDouble(), anyLong())).thenReturn(cobrancaSucesso);

        when(equipamentoService.destrancarTranca(10L)).thenReturn(true);

        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> {
            Aluguel a = i.getArgument(0);
            a.setId(500L);
            return a;
        });

        AluguelDTO resultado = service.realizarAluguel(novoAluguelDTO);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getBicicletaId());

        verify(aluguelRepository).save(any(Aluguel.class));
        verify(emailService).enviarEmail(eq("teste@email.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("[E1] Deve falhar se ciclista já tem aluguel ativo")
    void deveFalharSeAluguelAtivo() {
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(true);

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.realizarAluguel(novoAluguelDTO)
        );

        assertEquals("Ciclista já possui um aluguel em andamento.", ex.getMessage());
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("[E2] Deve falhar se não houver bicicleta na tranca")
    void deveFalharSeTrancaVazia() {
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(false);
        when(equipamentoService.buscarBicicletaNaTranca(10L)).thenReturn(null);

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.realizarAluguel(novoAluguelDTO)
        );

        assertEquals("Não existe bicicleta na tranca informada.", ex.getMessage());
    }

    @Test
    @DisplayName("Deve falhar aluguel se Ciclista não for encontrado")
    void deveFalharSeCiclistaNaoExiste() {
        // Simula que não achou ninguém com ID 1
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.empty());

        // Ajuste a classe da exceção conforme o que seu código lança (DadosInvalidos ou RecursoNaoEncontrado)
        assertThrows(Exception.class, () ->
                service.realizarAluguel(novoAluguelDTO)
        );
    }

    @Test
    @DisplayName("[E3] Deve falhar se pagamento não for autorizado (Cobrança retorna erro ou nulo)")
    void deveFalharSePagamentoReprovado() {
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(false);
        when(equipamentoService.buscarBicicletaNaTranca(10L)).thenReturn(bicicletaDisponivel);

        when(externoClient.realizarCobranca(anyDouble(), anyLong())).thenThrow(new DadosInvalidosException("Pagamento não autorizado"));

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.realizarAluguel(novoAluguelDTO)
        );

        assertTrue(ex.getMessage().contains("Pagamento não autorizado"));
        verify(equipamentoService, never()).destrancarTranca(anyLong());
    }

    @Test
    @DisplayName("[E4] Deve falhar se bicicleta estiver EM REPARO")
    void deveFalharSeBicicletaEmReparo() {
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(false);

        BicicletaDTO bikeQuebrada = new BicicletaDTO(200L, "EM_REPARO");
        when(equipamentoService.buscarBicicletaNaTranca(10L)).thenReturn(bikeQuebrada);

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.realizarAluguel(novoAluguelDTO)
        );

        assertTrue(ex.getMessage().contains("em reparo"));
    }

    @Test
    @DisplayName("[E5] Deve falhar se a tranca não abrir (Problema físico)")
    void deveFalharSeTrancaNaoAbrir() {
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(false);
        when(equipamentoService.buscarBicicletaNaTranca(10L)).thenReturn(bicicletaDisponivel);

        CobrancaDTO cobrancaMock = new CobrancaDTO();
        cobrancaMock.setStatus("OK");
        when(externoClient.realizarCobranca(anyDouble(), anyLong())).thenReturn(cobrancaMock);

        when(equipamentoService.destrancarTranca(10L)).thenReturn(false);

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.realizarAluguel(novoAluguelDTO)
        );

        assertTrue(ex.getMessage().contains("Erro ao destrancar"));
    }


    void setupDevolucao() {
        devolucaoDTO = new DevolucaoDTO();
        devolucaoDTO.setCiclistaId(1L);
        devolucaoDTO.setTrancaFimId(20L);
        devolucaoDTO.setDefeito(false);

        aluguelAtivo = new Aluguel();
        aluguelAtivo.setId(500L);
        aluguelAtivo.setCiclista(ciclistaAtivo);
        aluguelAtivo.setBicicleta(100L);
        aluguelAtivo.setTrancaInicio(10L);
        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusMinutes(30));
        aluguelAtivo.setValorExtra(0.0);
    }

    @Test
    @DisplayName("Deve realizar devolução sem custo extra (tempo < 2h)")
    void deveRealizarDevolucaoSemMulta() {
        setupDevolucao();
        when(aluguelRepository.findByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(Optional.of(aluguelAtivo));
        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> i.getArgument(0));

        AluguelDTO resultado = service.realizarDevolucao(devolucaoDTO);

        assertNotNull(resultado.getDataHoraFim());
        assertEquals(20L, resultado.getTrancaFimId());

        // Não deve chamar cobrança extra
        verify(externoClient, never()).realizarCobranca(anyDouble(), anyLong());

        // Verifica bike disponivel
        verify(equipamentoService).alterarStatusBicicleta(100L, "DISPONIVEL");

        verify(equipamentoService).trancarTranca(20L, 100L);
    }

    @Test
    @DisplayName("[R1] Deve cobrar multa corretamente (2h e 01min de uso)")
    void deveCobrarMultaPorUmMinutoExtra() {
        setupDevolucao();
        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusMinutes(121));

        when(aluguelRepository.findByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(Optional.of(aluguelAtivo));
        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> i.getArgument(0));

        // Mock pagamento OK
        CobrancaDTO cobrancaMock = new CobrancaDTO();
        cobrancaMock.setStatus("PAGA");
        when(externoClient.realizarCobranca(eq(5.00), eq(1L))).thenReturn(cobrancaMock);

        service.realizarDevolucao(devolucaoDTO);

        verify(externoClient).realizarCobranca(eq(5.00), eq(1L));
        assertEquals(5.00, aluguelAtivo.getValorExtra());
    }

    @Test
    @DisplayName("[A2] Deve registrar pendência se pagamento da multa falhar")
    void deveRegistrarPendenciaSeFalharPagamento() {
        setupDevolucao();
        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusMinutes(130)); // R$ 5,00 extra

        when(aluguelRepository.findByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(Optional.of(aluguelAtivo));
        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> i.getArgument(0));

        when(externoClient.realizarCobranca(anyDouble(), anyLong())).thenThrow(new DadosInvalidosException("Falha no pagamento"));


        assertDoesNotThrow(() -> service.realizarDevolucao(devolucaoDTO));

        // A devolução deve ocorrer mesmo com erro no pagamento
        verify(aluguelRepository).save(any(Aluguel.class));
        verify(equipamentoService).trancarTranca(20L, 100L);
    }

    @Test
    @DisplayName("Deve falhar se não houver aluguel ativo para devolver")
    void deveFalharSeSemAluguelAtivo() {
        setupDevolucao();
        when(aluguelRepository.findByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () ->
                service.realizarDevolucao(devolucaoDTO)
        );
    }


    @Test
    @DisplayName("Deve PERMITIR aluguel se ciclista está ativo e sem aluguel pendente")
    void devePermitirAluguel() {
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(false);

        assertTrue(ciclistaService.permiteAluguel(1L));
    }

    @Test
    @DisplayName("NÃO deve permitir aluguel se ciclista estiver inativo")
    void naoDevePermitirAluguelSeInativo() {
        ciclistaAtivo.setStatus(StatusCiclista.AGUARDANDO_CONFIRMACAO);
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));

        assertFalse(ciclistaService.permiteAluguel(1L));
    }

    @Test
    @DisplayName("Deve restaurar banco")
    void deveRestaurarBanco() {
        service.restaurarBanco();
        verify(aluguelRepository).deleteAll();
        verify(ciclistaRepository).deleteAll();
    }

    @Test
    @DisplayName("Deve finalizar aluguel com sucesso MESMO se o envio de e-mail falhar")
    void deveAlugarMesmoSemEmail() {
        // 1. Setup do Happy Path (igual ao deveRealizarAluguelComSucesso)
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(false);
        when(equipamentoService.buscarBicicletaNaTranca(10L)).thenReturn(bicicletaDisponivel);

        CobrancaDTO cobrancaOk = new CobrancaDTO();
        cobrancaOk.setStatus("OK");
        when(externoClient.realizarCobranca(anyDouble(), anyLong())).thenReturn(cobrancaOk);

        when(equipamentoService.destrancarTranca(10L)).thenReturn(true);
        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> i.getArgument(0));

        // 2. O PULO DO GATO: Simular erro no envio de e-mail
        // O Mockito vai lançar erro quando o service tentar mandar email
        doThrow(new RuntimeException("Servidor de email offline"))
                .when(emailService).enviarEmail(anyString(), anyString(), anyString());

        // 3. Execução: Não deve lançar exceção para o usuário
        AluguelDTO resultado = service.realizarAluguel(novoAluguelDTO);

        assertNotNull(resultado);
        // Verifica se salvou o aluguel mesmo com erro no email
        verify(aluguelRepository).save(any(Aluguel.class));
    }

    @Test
    @DisplayName("Deve finalizar devolução com sucesso MESMO se o envio de e-mail falhar")
    void deveDevolverMesmoSemEmail() {
        setupDevolucao();
        when(aluguelRepository.findByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(Optional.of(aluguelAtivo));
        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> i.getArgument(0));

        // Força erro no email na hora da devolução
        doThrow(new RuntimeException("Erro SMTP"))
                .when(emailService).enviarEmail(anyString(), anyString(), anyString());

        AluguelDTO resultado = service.realizarDevolucao(devolucaoDTO);

        assertNotNull(resultado.getDataHoraFim());
        verify(equipamentoService).alterarStatusBicicleta(anyLong(), eq("DISPONIVEL"));
    }
}