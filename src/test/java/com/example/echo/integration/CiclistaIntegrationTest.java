package com.example.echo.integration;

import com.example.echo.dto.CiclistaDTO;
import com.example.echo.dto.CiclistaPostDTO;
import com.example.echo.model.Ciclista;
import com.example.echo.model.Nacionalidade;
import com.example.echo.repository.CiclistaRepository;
import com.example.echo.service.EmailService;
import com.example.echo.service.externo.EquipamentoClient;
import com.example.echo.service.externo.ExternoClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class CiclistaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CiclistaRepository ciclistaRepository;

    @MockBean
    private EmailService emailService; // Mock para não enviar email real

    @MockBean
    private ExternoClient externoClient; // Mock para não chamar API externa

    @MockBean
    private EquipamentoClient equipamentoClient;

    @BeforeEach
    void setup() {
        ciclistaRepository.deleteAll();
    }

    @Test
    @DisplayName("INTEGRAÇÃO: Deve cadastrar ciclista com sucesso")
    void deveCadastrarCiclista() throws Exception {
        CiclistaPostDTO novoCiclista = new CiclistaPostDTO();
        novoCiclista.setNome("Novo User");
        novoCiclista.setEmail("novo@teste.com");
        novoCiclista.setNacionalidade(Nacionalidade.BRASILEIRO);
        novoCiclista.setNascimento(LocalDate.of(1995, 5, 5));
        novoCiclista.setSenha("123456");
        novoCiclista.setCpf("00011122233"); // Se seu DTO tiver CPF

        mockMvc.perform(post("/ciclista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoCiclista)))
                .andExpect(status().isCreated()) // ou isOk() dependendo do seu controller
                .andExpect(jsonPath("$.nome").value("Novo User"));
    }

    @Test
    @DisplayName("INTEGRAÇÃO: Deve buscar ciclista por ID")
    void deveBuscarCiclista() throws Exception {
        // 1. Salva direto no banco
        Ciclista c = new Ciclista();
        c.setNome("Buscado");
        c.setEmail("busca@teste.com");
        c.setSenha("123");
        c.setCpf("99988877766"); // Campos obrigatórios da entidade
        c.setNacionalidade(Nacionalidade.BRASILEIRO);
        c.setNascimento(LocalDate.now());
        Ciclista salvo = ciclistaRepository.save(c);

        // 2. Chama a API GET /ciclista/{id}
        mockMvc.perform(get("/ciclista/" + salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("busca@teste.com"));
    }

    @Test
    @DisplayName("INTEGRAÇÃO: Deve retornar 404 para ciclista inexistente")
    void deveRetornar404() throws Exception {
        mockMvc.perform(get("/ciclista/99999"))
                .andExpect(status().isNotFound());
    }
}