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

//testa apenas handler e controller
@WebMvcTest(controllers = {FuncionarioController.class, GlobalHandlerException.class})
class FuncionarioControllerTest {

    //cliente falso
    @Autowired
    private MockMvc mockMvc;

    //serviço falso
    @MockBean
    private FuncionarioService service;

    //conversor para json
    @Autowired
    private ObjectMapper objectMapper;

    //variáveis de teste
    private NovoFuncionarioDTO novoFuncionarioDTO;
    private FuncionarioDTO funcionarioDTO;
    private Long idExistente = 1L;
    private Long idInexistente = 99L;

    @BeforeEach
    void setUp() {
        //entradas
        novoFuncionarioDTO = new NovoFuncionarioDTO();
        novoFuncionarioDTO.setNome("João Teste");
        novoFuncionarioDTO.setEmail("joao@teste.com");
        novoFuncionarioDTO.setCpf("12345678901");
        novoFuncionarioDTO.setFuncao(FuncaoFuncionario.ADMINISTRATIVO);
        novoFuncionarioDTO.setIdade(30);
        novoFuncionarioDTO.setSenha("123");
        novoFuncionarioDTO.setConfirmacaoSenha("123");

        //saidas
        funcionarioDTO = new FuncionarioDTO();
        funcionarioDTO.setMatricula(idExistente);
        funcionarioDTO.setNome("João Teste");
        funcionarioDTO.setEmail("joao@teste.com");
    }

    @Test
    @DisplayName("POST /funcionario - Deve cadastrar e retornar 200 OK")
    void cadastrar() throws Exception {
        //o que retornar
        when(service.cadastrarFuncionario(any(NovoFuncionarioDTO.class))).thenReturn(funcionarioDTO);


        //simula o POST
        mockMvc.perform(post("/funcionario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoFuncionarioDTO)))
                //Verifica se a resposta foi 200 OK
                .andExpect(status().isOk())
                //Verifica se o JSON de resposta tem os campos corretos
                .andExpect(jsonPath("$.matricula").value(idExistente))
                .andExpect(jsonPath("$.nome").value("João Teste"));
    }

    @Test
    @DisplayName("POST /funcionario - Deve retornar 400 Bad Request por falha na validação")
    void cadastrarComErroDeValidacao() throws Exception {

        NovoFuncionarioDTO dtoInvalido = new NovoFuncionarioDTO();
        dtoInvalido.setNome("João Teste");
        dtoInvalido.setEmail("joao@teste.com");
        dtoInvalido.setCpf("12345678901");
        dtoInvalido.setFuncao(FuncaoFuncionario.ADMINISTRATIVO);
        dtoInvalido.setIdade(30);
        dtoInvalido.setConfirmacaoSenha("123");

        dtoInvalido.setSenha(""); //falha no @NotBlank


        mockMvc.perform(post("/funcionario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                // Verifica se o status é 422
                .andExpect(status().isUnprocessableEntity())
                // Verifica se a resposta de erro (do GlobalExceptionHandler) está correta
                .andExpect(jsonPath("$.codigo").value("422 UNPROCESSABLE_ENTITY"))
                .andExpect(jsonPath("$.mensagem").value("Campo obrigatório"));
    }

    @Test
    @DisplayName("GET /funcionario/{id} - Deve buscar por ID e retornar 200 OK")
    void buscarPorId() throws Exception {

        when(service.buscarFuncionarioPorId(idExistente)).thenReturn(funcionarioDTO);

        mockMvc.perform(get("/funcionario/{id}", idExistente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula").value(idExistente));
    }

    @Test
    @DisplayName("GET /funcionario/{id} - Deve retornar 404 Not Found ao buscar ID inexistente")
    void buscarPorIdInexistente() throws Exception {

        when(service.buscarFuncionarioPorId(idInexistente))
                .thenThrow(new RecursoNaoEncontradoException("Funcionário não encontrado"));

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

        when(service.listarFuncionarios()).thenReturn(Collections.singletonList(funcionarioDTO));

        mockMvc.perform(get("/funcionario"))
                .andExpect(status().isOk())
                //Verifica se a resposta é um array e se o primeiro item tem a matrícula
                .andExpect(jsonPath("$[0].matricula").value(idExistente));
    }

    @Test
    @DisplayName("PUT /funcionario/{id} - Deve atualizar e retornar 200 OK")
    void atualizar() throws Exception {

        when(service.atualizarFuncionario(eq(idExistente), any(NovoFuncionarioDTO.class))).thenReturn(funcionarioDTO);


        mockMvc.perform(put("/funcionario/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoFuncionarioDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula").value(idExistente));
    }

    @Test
    @DisplayName("DELETE /funcionario/{id} - Deve deletar e retornar 200 No Content")
    void deletar() throws Exception {

        doNothing().when(service).deletarFuncionario(idExistente);

        mockMvc.perform(delete("/funcionario/{id}", idExistente))
                .andExpect(status().isOk()); // O controller retorna 200
    }
}