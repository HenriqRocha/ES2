package com.example.echo.controller;

import com.example.echo.service.AluguelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DadosController.class)
class DadosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AluguelService aluguelService;

    @Test
    @DisplayName("GET /restaurarBanco - Deve retornar 200 OK")
    void deveRestaurarBancoComSucesso() throws Exception {
        doNothing().when(aluguelService).restaurarBanco();

        mockMvc.perform(get("/restaurarBanco"))
                .andExpect(status().isOk());
    }
}