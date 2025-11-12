package com.example.echo.controller;

import com.example.echo.dto.FuncionarioDTO;
import com.example.echo.dto.NovoFuncionarioDTO;
import com.example.echo.exception.GlobalHandlerException;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.model.FuncaoFuncionario;
import com.example.echo.service.FuncionarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 1. Diz ao Spring para testar APENAS o Controller e o nosso Handler
@WebMvcTest(controllers = {FuncionarioController.class, GlobalHandlerException.class})
class FuncionarioControllerTest {

    // 2. O "cliente" falso para fazer requisições HTTP
    @Autowired
    private MockMvc mockMvc;

    // 3. O "serviço" falso que o controller vai usar
    @MockBean
    private FuncionarioService service;

    // 4. O conversor de Java para JSON
    @Autowired
    private ObjectMapper objectMapper;

    // Variáveis de teste
    private NovoFuncionarioDTO novoFuncionarioDTO;
    private FuncionarioDTO funcionarioDTO;
    private Long idExistente = 1L;
    private Long idInexistente = 99L;

    @BeforeEach
    void setUp() {
        // DTO de entrada (para POST/PUT)
        novoFuncionarioDTO = new NovoFuncionarioDTO();
        novoFuncionarioDTO.setNome("João Teste");
        novoFuncionarioDTO.setEmail("joao@teste.com");
        novoFuncionarioDTO.setCpf("12345678901");
        novoFuncionarioDTO.setFuncao(FuncaoFuncionario.ADMINISTRATIVO);
        novoFuncionarioDTO.setIdade(30);
        novoFuncionarioDTO.setSenha("123");
        novoFuncionarioDTO.setConfirmacaoSenha("123");

        // DTO de saída (o que o serviço retorna)
        funcionarioDTO = new FuncionarioDTO();
        funcionarioDTO.setMatricula(idExistente);
        funcionarioDTO.setNome("João Teste");
        funcionarioDTO.setEmail("joao@teste.com");
        // ... (etc.)
    }

    @Test
    @DisplayName("POST /funcionario - Deve cadastrar e retornar 200 OK")
    void cadastrar() throws Exception {
        // ARRANGE
        // Diz ao "serviço falso" o que retornar
        when(service.cadastrarFuncionario(any(NovoFuncionarioDTO.class))).thenReturn(funcionarioDTO);

        // ACT & ASSERT
        // Simula o POST
        mockMvc.perform(post("/funcionario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoFuncionarioDTO)))
                // Verifica se a resposta foi 200 OK
                .andExpect(status().isOk())
                // Verifica se o JSON de resposta tem os campos corretos
                .andExpect(jsonPath("$.matricula").value(idExistente))
                .andExpect(jsonPath("$.nome").value("João Teste"));
    }

    @Test
    @DisplayName("POST /funcionario - Deve retornar 400 Bad Request por falha na validação")
    void cadastrarComErroDeValidacao() throws Exception {
        // ARRANGE
        // Cria um DTO inválido (sem nome)
        NovoFuncionarioDTO dtoInvalido = new NovoFuncionarioDTO();
        dtoInvalido.setEmail("email@valido.com");

        // ACT & ASSERT
        // Simula o POST
        mockMvc.perform(post("/funcionario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                // Verifica se o status é 400 (Bad Request)
                .andExpect(status().isBadRequest())
                // Verifica se a resposta de erro (do GlobalExceptionHandler) está correta
                .andExpect(jsonPath("$.codigo").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.mensagem").value("Senha é obrigatória")); // A primeira regra @NotBlank do DTO
    }

    @Test
    @DisplayName("GET /funcionario/{id} - Deve buscar por ID e retornar 200 OK")
    void buscarPorId() throws Exception {
        // ARRANGE
        when(service.buscarFuncionarioPorId(idExistente)).thenReturn(funcionarioDTO);

        // ACT & ASSERT
        mockMvc.perform(get("/funcionario/{id}", idExistente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula").value(idExistente));
    }

    @Test
    @DisplayName("GET /funcionario/{id} - Deve retornar 404 Not Found ao buscar ID inexistente")
    void buscarPorIdInexistente() throws Exception {
        // ARRANGE
        // Simula o "serviço" lançando a exceção que criámos
        when(service.buscarFuncionarioPorId(idInexistente))
                .thenThrow(new RecursoNaoEncontradoException("Funcionário não encontrado"));

        // ACT & ASSERT
        mockMvc.perform(get("/funcionario/{id}", idInexistente))
                // Verifica se o status é 404
                .andExpect(status().isNotFound())
                // Verifica se o GlobalExceptionHandler formatou o erro
                .andExpect(jsonPath("$.codigo").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.mensagem").value("Funcionário não encontrado"));
    }

    @Test
    @DisplayName("GET /funcionario - Deve listar todos e retornar 200 OK")
    void listarTodos() throws Exception {
        // ARRANGE
        when(service.listarFuncionarios()).thenReturn(Collections.singletonList(funcionarioDTO));
        // ACT & ASSERT
        mockMvc.perform(get("/funcionario"))
                .andExpect(status().isOk())
                // Verifica se a resposta é um array ([...]) e se o primeiro item tem a matrícula
                .andExpect(jsonPath("$[0].matricula").value(idExistente));
    }

    @Test
    @DisplayName("PUT /funcionario/{id} - Deve atualizar e retornar 200 OK")
    void atualizar() throws Exception {
        // ARRANGE
        when(service.atualizarFuncionario(eq(idExistente), any(NovoFuncionarioDTO.class))).thenReturn(funcionarioDTO);

        // ACT & ASSERT
        mockMvc.perform(put("/funcionario/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoFuncionarioDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula").value(idExistente));
    }

    @Test
    @DisplayName("DELETE /funcionario/{id} - Deve deletar e retornar 204 No Content")
    void deletar() throws Exception {
        // ARRANGE
        // O método 'deletar' não retorna nada (void)
        doNothing().when(service).deletarFuncionario(idExistente);

        // ACT & ASSERT
        mockMvc.perform(delete("/funcionario/{id}", idExistente))
                .andExpect(status().isNoContent()); // O controller retorna 204
    }
}