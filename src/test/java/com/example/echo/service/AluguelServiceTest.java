package com.example.echo.service;

import com.example.echo.dto.AluguelDTO;
import com.example.echo.dto.BicicletaDTO;
import com.example.echo.dto.NovoAluguelDTO;
import com.example.echo.dto.DevolucaoDTO;
import java.time.temporal.ChronoUnit;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.model.Aluguel;
import com.example.echo.model.CartaoDeCredito;
import com.example.echo.model.Ciclista;
import com.example.echo.model.StatusCiclista;
import com.example.echo.repository.AluguelRepository;
import com.example.echo.repository.CiclistaRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AluguelServiceTest {

    @Mock private AluguelRepository aluguelRepository;
    @Mock private CiclistaRepository ciclistaRepository;
    @Mock private EmailService emailService;
    @Mock private EquipamentoService equipamentoService;
    @Mock private CobrancaService cobrancaService;

    @InjectMocks
    private AluguelService service;

    @InjectMocks CiclistaService ciclistaService;

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
        //Mocks de Sucesso
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(false); // Sem aluguel ativo

        when(equipamentoService.buscarBicicletaNaTranca(10L)).thenReturn(bicicletaDisponivel);
        when(cobrancaService.autorizarPagamento(anyString(), anyDouble())).thenReturn(true); // Pagamento OK
        when(equipamentoService.destrancarTranca(10L)).thenReturn(true); // Tranca abriu

        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> {
            Aluguel a = i.getArgument(0);
            a.setId(500L); //Simula ID
            return a;
        });

        AluguelDTO resultado = service.realizarAluguel(novoAluguelDTO);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getBicicletaId());
        assertEquals(10L, resultado.getTrancaInicioId());

        // Verifica se chamou o save e o email
        verify(aluguelRepository).save(any(Aluguel.class));
        verify(emailService).enviarEmail(eq("teste@email.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("[E1] Deve falhar se ciclista já tem aluguel ativo")
    void deveFalharSeAluguelAtivo() {
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        //ja tem aluduel
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(true);

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.realizarAluguel(novoAluguelDTO)
        );

        assertEquals("Ciclista já possui um aluguel em andamento.", ex.getMessage());
        //verifica notificação
        verify(emailService).enviarEmail(eq("teste@email.com"), anyString(), contains("já possui uma bicicleta"));
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("[E2] Deve falhar se não houver bicicleta na tranca")
    void deveFalharSeTrancaVazia() {
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(false);

        //Retorna NULL (Tranca vazia)
        when(equipamentoService.buscarBicicletaNaTranca(10L)).thenReturn(null);

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.realizarAluguel(novoAluguelDTO)
        );

        assertEquals("Não existe bicicleta na tranca informada.", ex.getMessage());
    }

    @Test
    @DisplayName("[E3] Deve falhar se pagamento não for autorizado e registrar pendência")
    void deveFalharSePagamentoReprovado() {
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(false);
        when(equipamentoService.buscarBicicletaNaTranca(10L)).thenReturn(bicicletaDisponivel);

        // Pagamento reprovado
        when(cobrancaService.autorizarPagamento(anyString(), anyDouble())).thenReturn(false);

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.realizarAluguel(novoAluguelDTO)
        );

        assertTrue(ex.getMessage().contains("Pagamento não autorizado"));

        //registra pendencia
        verify(cobrancaService).registrarCobrancaPendente(eq(1L), eq(10.00));
        //Garante que não destrancou a tranca
        verify(equipamentoService, never()).destrancarTranca(anyLong());
    }

    @Test
    @DisplayName("[E4] Deve falhar se bicicleta estiver EM REPARO")
    void deveFalharSeBicicletaEmReparo() {
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(false);

        // |Bike quebrada
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
        when(cobrancaService.autorizarPagamento(anyString(), anyDouble())).thenReturn(true);

        //Tranca travada (retorna false)
        when(equipamentoService.destrancarTranca(10L)).thenReturn(false);

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.realizarAluguel(novoAluguelDTO)
        );

        assertTrue(ex.getMessage().contains("Erro ao destrancar"));
        verify(aluguelRepository, never()).save(any());
    }

    @Test
    @DisplayName("[R1] Deve falhar se ciclista não estiver ATIVO")
    void deveFalharSeCiclistaInativo() {
        ciclistaAtivo.setStatus(StatusCiclista.AGUARDANDO_CONFIRMACAO); // Status inválido para alugar
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.realizarAluguel(novoAluguelDTO)
        );

        assertEquals("Cadastro do ciclista não está ativo.", ex.getMessage());
    }

    //setUp devolução
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
        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusMinutes(30)); //começou 30min atrás
        aluguelAtivo.setValorExtra(0.0);
    }

    @Test
    @DisplayName("Deve realizar devolução sem custo extra (tempo < 2h)")
    void deveRealizarDevolucaoSemMulta() {
        setupDevolucao();
        // encontra aluguel
        when(aluguelRepository.findByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(Optional.of(aluguelAtivo));

        //Save retorna o próprio objeto
        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> i.getArgument(0));

        AluguelDTO resultado = service.realizarDevolucao(devolucaoDTO);

        assertNotNull(resultado.getDataHoraFim()); // Data fim foi preenchida
        assertEquals(20L, resultado.getTrancaFimId()); // Tranca fim registrada

        // Verifica se o valor extra ficou 0.0 porque não passou 30min
        verify(cobrancaService, never()).autorizarPagamento(anyString(), anyDouble());

        // Verifica se a bike ficou disponivel
        verify(equipamentoService).alterarStatusBicicleta(100L, "DISPONIVEL");

        // Verifica se trancou a tranca
        verify(equipamentoService).trancarTranca(20L);
    }

    @Test
    @DisplayName("[R1] Deve cobrar multa corretamente (2h e 01min de uso)")
    void deveCobrarMultaPorUmMinutoExtra() {
        setupDevolucao();
        // Simulamos que o aluguel começou há 121 minutos
        // Passou de 2h -> Cobra 1 bloco de 30min -> 5 conto
        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusMinutes(121));

        when(aluguelRepository.findByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(Optional.of(aluguelAtivo));
        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> i.getArgument(0));

        //Pagamento Autorizado
        when(cobrancaService.autorizarPagamento(anyString(), eq(5.00))).thenReturn(true);

        service.realizarDevolucao(devolucaoDTO);

        // Verifica se tentou cobrar exatamente 5.00
        verify(cobrancaService).autorizarPagamento(anyString(), eq(5.00));

        // O valor extra no objeto deve ser 5.00
        assertEquals(5.00, aluguelAtivo.getValorExtra());
    }

    @Test
    @DisplayName("[R1] Deve cobrar multa acumulada (2h e 31min de uso)")
    void deveCobrarMultaPorDoisBlocosExtras() {
        setupDevolucao();
        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusMinutes(151));

        when(aluguelRepository.findByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(Optional.of(aluguelAtivo));
        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> i.getArgument(0));
        when(cobrancaService.autorizarPagamento(anyString(), eq(10.00))).thenReturn(true);

        service.realizarDevolucao(devolucaoDTO);

        verify(cobrancaService).autorizarPagamento(anyString(), eq(10.00));
        assertEquals(10.00, aluguelAtivo.getValorExtra());
    }

    @Test
    @DisplayName("[A2] Deve registrar pendência se pagamento da multa falhar")
    void deveRegistrarPendenciaSeFalharPagamento() {
        setupDevolucao();
        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusMinutes(130)); // 10 min extra -> R$ 5,00

        when(aluguelRepository.findByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(Optional.of(aluguelAtivo));
        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> i.getArgument(0));

        //Pagamento REPROVADO
        when(cobrancaService.autorizarPagamento(anyString(), anyDouble())).thenReturn(false);

        service.realizarDevolucao(devolucaoDTO);

        // Verifica se chamou o registro de pendência
        verify(cobrancaService).registrarCobrancaPendente(eq(1L), eq(5.00));

        // A devolução deve ocorrer mesmo assim
        verify(aluguelRepository).save(any(Aluguel.class));
    }

    @Test
    @DisplayName("[A3] Deve alterar status para EM_REPARO se ciclista informar defeito")
    void deveMarcarReparoSeInformado() {
        setupDevolucao();
        devolucaoDTO.setDefeito(true); // [A3] Usuário marcou reparo

        when(aluguelRepository.findByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(Optional.of(aluguelAtivo));
        when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> i.getArgument(0));

        service.realizarDevolucao(devolucaoDTO);

        // Verifica se chamou o serviço externo mudando status para EM_REPARO
        verify(equipamentoService).alterarStatusBicicleta(100L, "EM_REPARO");
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
        //Ciclista existe e ta ativo
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        //  Não tem aluguel aberto
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
    @DisplayName("NÃO deve permitir aluguel se já houver aluguel em andamento")
    void naoDevePermitirAluguelSeJaTemUm() {
        when(ciclistaRepository.findById(1L)).thenReturn(Optional.of(ciclistaAtivo));
        //ja tem
        when(aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(true);

        assertFalse(ciclistaService.permiteAluguel(1L));
    }

    @Test
    @DisplayName("Deve retornar bicicleta alugada se houver aluguel ativo")
    void deveRetornarBicicletaAlugada() {
        when(ciclistaRepository.existsById(1L)).thenReturn(true);

        // Mock do aluguel ativo com bicicleta ID 100
        Aluguel aluguel = new Aluguel();
        aluguel.setBicicleta(100L);
        when(aluguelRepository.findByCiclistaIdAndHoraFimIsNull(1L)).thenReturn(Optional.of(aluguel));

        BicicletaDTO dto = ciclistaService.buscarBicicletaAlugada(1L);

        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals("EM_USO", dto.getStatus());
    }

    @Test
    @DisplayName("Deve restaurar banco (apagar tudo)")
    void deveRestaurarBanco() {
        service.restaurarBanco();

        // Verifica se chamou o deleteAll nos repositórios
        verify(aluguelRepository).deleteAll();
        verify(ciclistaRepository).deleteAll();
    }
}