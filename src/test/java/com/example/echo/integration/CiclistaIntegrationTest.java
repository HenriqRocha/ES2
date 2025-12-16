package com.example.echo.integration;

import com.example.echo.dto.CiclistaDTO;
import com.example.echo.dto.CiclistaPostDTO;
import com.example.echo.model.Ciclista;
import com.example.echo.model.Nacionalidade;
import com.example.echo.model.StatusCiclista;
import com.example.echo.repository.CiclistaRepository;
import com.example.echo.service.EmailService;
import com.example.echo.service.externo.ExternoClient;
import com.example.echo.service.externo.EquipamentoClient;
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
    private EmailService emailService;

    @MockBean
    private ExternoClient externoClient;

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

        // CORREÇÃO 1: CPF válido (gerado) e Senha forte/confirmada
        novoCiclista.setCpf("92626464083");
        novoCiclista.setSenha("SenhaForte123");
        novoCiclista.setConfirmacaoSenha("SenhaForte123");
        novoCiclista.setUrlFotoDocumento("http://url.com/foto.png");

        mockMvc.perform(post("/ciclista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoCiclista)))
                .andExpect(status().isCreated()) // Espera 201
                .andExpect(jsonPath("$.nome").value("Novo User"));
    }

    @Test
    @DisplayName("INTEGRAÇÃO: Deve buscar ciclista por ID")
    void deveBuscarCiclista() throws Exception {
        // CORREÇÃO 2: Preencher TODOS os campos obrigatórios da entidade
        Ciclista c = new Ciclista();
        c.setNome("Buscado");
        c.setEmail("busca@teste.com");
        c.setSenha("123");
        c.setCpf("02263445032"); // Outro CPF válido diferente do de cima
        c.setNacionalidade(Nacionalidade.BRASILEIRO);
        c.setNascimento(LocalDate.now());

        // Esses campos geralmente causam o DataIntegrityViolation se forem nulos:
        c.setStatus(StatusCiclista.ATIVO);
        c.setUrlFotoDocumento("http://foto.com");

        Ciclista salvo = ciclistaRepository.save(c);

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