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

    @Test
    @DisplayName("Deve retornar bicicleta se estiver na tranca")
    void deveBuscarBicicletaNaTranca() {
        // Cenario
        TrancaDTO tranca = new TrancaDTO();
        tranca.setId(10L);
        tranca.setBicicleta(200L);

        BicicletaDTO bike = new BicicletaDTO(200L, "DISPONIVEL");

        when(equipamentoClient.buscarTranca(10L)).thenReturn(tranca);
        when(equipamentoClient.buscarBicicleta(200L)).thenReturn(bike);

        // Acao
        BicicletaDTO resultado = service.buscarBicicletaNaTranca(10L);

        // Verificacao
        assertNotNull(resultado);
        assertEquals(200L, resultado.getId());
    }

    @Test
    @DisplayName("Deve retornar NULL se tranca estiver vazia")
    void deveRetornarNullSeTrancaVazia() {
        TrancaDTO trancaVazia = new TrancaDTO();
        trancaVazia.setId(10L);
        trancaVazia.setBicicleta(null); // Vazia

        when(equipamentoClient.buscarTranca(10L)).thenReturn(trancaVazia);

        BicicletaDTO resultado = service.buscarBicicletaNaTranca(10L);
        assertNull(resultado);
    }

    @Test
    @DisplayName("Deve lançar erro se falhar comunicação na busca")
    void deveLancarErroSeFalharBusca() {
        when(equipamentoClient.buscarTranca(10L)).thenThrow(new RuntimeException("API fora"));

        assertThrows(RecursoNaoEncontradoException.class, () ->
                service.buscarBicicletaNaTranca(10L)
        );
    }

    @Test
    @DisplayName("Deve destrancar com sucesso")
    void deveDestrancar() {
        TrancaDTO tranca = new TrancaDTO();
        tranca.setBicicleta(100L);
        when(equipamentoClient.buscarTranca(1L)).thenReturn(tranca);

        boolean result = service.destrancarTranca(1L);

        assertTrue(result);
        verify(equipamentoClient).destrancarTranca(1L, 100L);
    }

    @Test
    @DisplayName("Deve lançar DadosInvalidosException ao falhar alteração de status")
    void deveTratarErroAlterarStatus() {
        doThrow(new RuntimeException("Erro")).when(equipamentoClient).alterarStatusBicicleta(anyLong(), anyString());

        assertThrows(DadosInvalidosException.class, () ->
                service.alterarStatusBicicleta(1L, "TESTE")
        );
    }
}