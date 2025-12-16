package com.example.echo.service;

import com.example.echo.dto.BicicletaDTO;
import com.example.echo.dto.externo.TrancaDTO;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.service.externo.EquipamentoClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipamentoServiceTest {

    @Mock
    private EquipamentoClient equipamentoClient;

    @InjectMocks
    private EquipamentoService service;

    // --- TESTES: buscarBicicletaNaTranca ---

    @Test
    @DisplayName("Deve retornar bicicleta se estiver na tranca (Sucesso)")
    void deveBuscarBicicletaNaTranca() {
        // Mock da Tranca
        TrancaDTO tranca = new TrancaDTO();
        tranca.setId(10L);
        tranca.setBicicleta(200L);

        // Mock da Bicicleta que será retornada
        BicicletaDTO bike = new BicicletaDTO(200L, "DISPONIVEL");

        when(equipamentoClient.buscarTranca(10L)).thenReturn(tranca);
        when(equipamentoClient.buscarBicicleta(200L)).thenReturn(bike);

        BicicletaDTO resultado = service.buscarBicicletaNaTranca(10L);

        assertNotNull(resultado);
        assertEquals(200L, resultado.getId());
        verify(equipamentoClient).buscarBicicleta(200L);
    }

    @Test
    @DisplayName("Deve retornar NULL se a tranca estiver vazia")
    void deveRetornarNullSeTrancaVazia() {
        TrancaDTO trancaVazia = new TrancaDTO();
        trancaVazia.setId(10L);
        trancaVazia.setBicicleta(null); // Sem bicicleta

        when(equipamentoClient.buscarTranca(10L)).thenReturn(trancaVazia);

        BicicletaDTO resultado = service.buscarBicicletaNaTranca(10L);

        assertNull(resultado);
        // Garante que não tentou buscar bicicleta, pois não tinha ID
        verify(equipamentoClient, never()).buscarBicicleta(anyLong());
    }

    @Test
    @DisplayName("Deve retornar NULL se a tranca for nula (não encontrada pelo client)")
    void deveRetornarNullSeTrancaNula() {
        when(equipamentoClient.buscarTranca(10L)).thenReturn(null);

        BicicletaDTO resultado = service.buscarBicicletaNaTranca(10L);

        assertNull(resultado);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException se der erro na API ao buscar")
    void deveLancarErroSeFalharBusca() {
        // Simula erro de conexão ou 404 do Client
        when(equipamentoClient.buscarTranca(10L)).thenThrow(new RuntimeException("API fora"));

        assertThrows(RecursoNaoEncontradoException.class, () ->
                service.buscarBicicletaNaTranca(10L)
        );
    }

    // --- TESTES: destrancarTranca ---

    @Test
    @DisplayName("Deve destrancar com sucesso")
    void deveDestrancar() {
        TrancaDTO tranca = new TrancaDTO();
        tranca.setId(1L);
        tranca.setBicicleta(100L);

        when(equipamentoClient.buscarTranca(1L)).thenReturn(tranca);

        boolean result = service.destrancarTranca(1L);

        assertTrue(result);
        verify(equipamentoClient).destrancarTranca(1L, 100L);
    }

    @Test
    @DisplayName("Deve retornar FALSE se não houver bicicleta para destrancar")
    void naoDeveDestrancarSeVazia() {
        TrancaDTO tranca = new TrancaDTO();
        tranca.setId(1L);
        tranca.setBicicleta(null); // Vazia

        when(equipamentoClient.buscarTranca(1L)).thenReturn(tranca);

        boolean result = service.destrancarTranca(1L);

        assertFalse(result);
        verify(equipamentoClient, never()).destrancarTranca(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Deve retornar FALSE e capturar exceção se a API falhar ao destrancar")
    void deveRetornarFalseSeDerErroAPI() {
        // Simula erro ao buscar tranca
        when(equipamentoClient.buscarTranca(1L)).thenThrow(new RuntimeException("Erro Conexão"));

        boolean result = service.destrancarTranca(1L);

        assertFalse(result); // Caiu no catch e retornou false
    }

    // --- TESTES: alterarStatusBicicleta ---

    @Test
    @DisplayName("Deve alterar status da bicicleta com sucesso")
    void deveAlterarStatusComSucesso() {
        service.alterarStatusBicicleta(100L, "EM_USO");
        verify(equipamentoClient).alterarStatusBicicleta(100L, "EM_USO");
    }

    @Test
    @DisplayName("Deve lançar DadosInvalidosException ao falhar alteração de status")
    void deveTratarErroAlterarStatus() {
        doThrow(new RuntimeException("Erro")).when(equipamentoClient).alterarStatusBicicleta(anyLong(), anyString());

        assertThrows(DadosInvalidosException.class, () ->
                service.alterarStatusBicicleta(1L, "TESTE")
        );
    }

    // --- TESTES: trancarTranca ---

    @Test
    @DisplayName("Deve trancar a tranca com sucesso")
    void deveTrancarTranca() {
        service.trancarTranca(10L, 200L);
        verify(equipamentoClient).trancarTranca(10L, 200L);
    }

    @Test
    @DisplayName("Deve tratar erro ao trancar a tranca (Catch)")
    void deveTratarErroAoTrancar() {
        doThrow(new RuntimeException("Falha hardware")).when(equipamentoClient).trancarTranca(anyLong(), anyLong());

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.trancarTranca(10L, 200L)
        );
        assertEquals("Falha ao travar a tranca na devolução.", ex.getMessage());
    }
}