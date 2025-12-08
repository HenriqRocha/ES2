package com.example.echo.controller;

import com.example.echo.dto.AluguelDTO;
import com.example.echo.dto.BicicletaDTO;
import com.example.echo.dto.NovoAluguelDTO;
import com.example.echo.dto.DevolucaoDTO;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.exception.GlobalHandlerException;
import com.example.echo.service.AluguelService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AluguelController.class, GlobalHandlerException.class, DevolucaoController.class})
class AluguelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AluguelService aluguelService;

    @MockBean
    private CiclistaService ciclistaService;

    @Autowired
    private ObjectMapper objectMapper;

    private NovoAluguelDTO novoAluguelDTO;
    private AluguelDTO aluguelDTO;

    @BeforeEach
    void setUp() {
        novoAluguelDTO = new NovoAluguelDTO();
        novoAluguelDTO.setCiclistaId(1L);
        novoAluguelDTO.setTrancaInicioId(10L);

        com.example.echo.model.Aluguel aluguelMock = new com.example.echo.model.Aluguel();
        aluguelMock.setId(100L);

        com.example.echo.model.Ciclista ciclistaMock = new com.example.echo.model.Ciclista();
        ciclistaMock.setId(1L);
        aluguelMock.setCiclista(ciclistaMock);

        aluguelMock.setBicicleta(100L);
        aluguelMock.setTrancaInicio(10L);
        aluguelMock.setCobranca(12345L);
        aluguelMock.setHoraInicio(java.time.LocalDateTime.now());

        aluguelDTO = new AluguelDTO(aluguelMock);
    }

    @Test
    @DisplayName("POST /aluguel - Deve realizar aluguel com sucesso (200 OK)")
    void deveRealizarAluguelComSucesso() throws Exception {
        when(aluguelService.realizarAluguel(any(NovoAluguelDTO.class))).thenReturn(aluguelDTO);

        mockMvc.perform(post("/aluguel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoAluguelDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.ciclistaId").value(1L));
    }

    @Test
    @DisplayName("POST /aluguel - Deve retornar 422 se serviço lançar DadosInvalidosException")
    void deveRetornar422SeHouverErroDeNegocio() throws Exception {
        when(aluguelService.realizarAluguel(any(NovoAluguelDTO.class)))
                .thenThrow(new DadosInvalidosException("Ciclista já possui aluguel"));

        mockMvc.perform(post("/aluguel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoAluguelDTO)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Ciclista já possui aluguel"));
    }

    @Test
    @DisplayName("POST /aluguel - Deve retornar 422 se DTO for inválido (Validação @NotNull)")
    void deveRetornar422SeDtoInvalido() throws Exception {
        NovoAluguelDTO invalido = new NovoAluguelDTO();
        //CiclistaID e TrancaID nulos

        mockMvc.perform(post("/aluguel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST/devolucao - Deve finalizar aluguel com sucesso (200 OK)")
    void deveRealizarDevolucaoComSucesso() throws Exception {
        DevolucaoDTO dto = new DevolucaoDTO();
        dto.setCiclistaId(1L);
        dto.setTrancaFimId(20L);

        //Mock do retorno do service (usando o objeto aluguelDTO que já ta no setUp)
        when(aluguelService.realizarDevolucao(any(DevolucaoDTO.class))).thenReturn(aluguelDTO);

        mockMvc.perform(post("/devolucao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L)); //Verifica ID do aluguel devolvido
    }

}