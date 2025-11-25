package com.example.echo.controller;

import com.example.echo.dto.CartaoDeCreditoDTO;
import com.example.echo.exception.GlobalHandlerException;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.service.CiclistaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {CartaoDeCreditoController.class, GlobalHandlerException.class})
class CartaoDeCreditoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CiclistaService service;

    @Autowired
    private ObjectMapper objectMapper;

    private CartaoDeCreditoDTO cartaoDTO;

    @BeforeEach
    void setUp() {
        cartaoDTO = new CartaoDeCreditoDTO();
        cartaoDTO.setNomeTitular("Teste Silva");
        cartaoDTO.setNumero("1234567812345678");
        cartaoDTO.setValidade(LocalDate.now().plusYears(1));
        cartaoDTO.setCvv("123");
    }

    @Test
    @DisplayName("GET /cartaoDeCredito/{id} - Deve retornar os dados do cartão")
    void deveRetornarCartao() throws Exception {
        Long id = 1L;
        when(service.buscarCartao(id)).thenReturn(cartaoDTO);

        mockMvc.perform(get("/cartaoDeCredito/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeTitular").value("Teste Silva"))
                .andExpect(jsonPath("$.numero").value("1234567812345678"));
    }

    @Test
    @DisplayName("PUT /cartaoDeCredito/{id} - Deve atualizar cartão e retornar 200 OK")
    void deveAtualizarCartao() throws Exception {
        Long id = 1L;

        // O método no service é void, então usamos doNothing (padrão do mockito para voids)
        // Mas podemos forçar para garantir
        doNothing().when(service).alterarCartao(eq(id), any(CartaoDeCreditoDTO.class));

        mockMvc.perform(put("/cartaoDeCredito/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartaoDTO)))
                .andExpect(status().isOk()); // Espera 200 OK sem corpo
    }

    @Test
    @DisplayName("GET /cartaoDeCredito/{id} - Deve retornar 404 se ciclista não existir")
    void deveRetornar404AoBuscarCartaoInexistente() throws Exception {
        when(service.buscarCartao(99L)).thenThrow(new RecursoNaoEncontradoException("Ciclista não encontrado"));

        mockMvc.perform(get("/cartaoDeCredito/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Ciclista não encontrado"));
    }
}