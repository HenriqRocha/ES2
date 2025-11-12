package com.example.echo.controller;

import com.example.echo.dto.CartaoDeCreditoDTO;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.exception.GlobalHandlerException;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.model.Nacionalidade;
import com.example.echo.model.StatusCiclista;
import com.example.echo.service.CiclistaService;
import com.example.echo.dto.CiclistaDTO;
import com.example.echo.dto.CiclistaPostDTO;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {CiclistaController.class, GlobalHandlerException.class})
class CiclistaControllerTest {

    @Autowired
    private MockMvc mockMvc; //Cliente falso

    @MockBean
    private CiclistaService service; //Serviço falso

    @Autowired
    private ObjectMapper objectMapper; //Para converter DTOs em JSON

    private CiclistaPostDTO ciclistaPostDTO;
    private CiclistaDTO ciclistaDTO;
    private String ciclistaPostDTOJson;

    @BeforeEach
    void setUp() throws Exception {
        // DTO de resposta do serviço
        ciclistaDTO = new CiclistaDTO();
        ciclistaDTO.setId(1L);
        ciclistaDTO.setEmail("teste.valido@email.com");
        ciclistaDTO.setNome("Ciclista Teste Válido");

        // DTO de entrada
        ciclistaPostDTO = new CiclistaPostDTO();
        ciclistaPostDTO.setNome("Ciclista Teste Válido");
        ciclistaPostDTO.setNascimento(LocalDate.of(1990, 5, 15));
        ciclistaPostDTO.setNacionalidade(Nacionalidade.BRASILEIRO);
        ciclistaPostDTO.setCpf("12345678901"); //R1
        ciclistaPostDTO.setEmail("teste.valido@email.com");
        ciclistaPostDTO.setSenha("senha123"); //R2
        ciclistaPostDTO.setConfirmacaoSenha("senha123");//R2)

        //meio de pagamento
        CartaoDeCreditoDTO cartaoDTO = new CartaoDeCreditoDTO();
        cartaoDTO.setNomeTitular("Ciclista Teste");
        cartaoDTO.setNumero("1111222233334444");
        cartaoDTO.setValidade(LocalDate.now().plusYears(2));
        cartaoDTO.setCvv("123");

        ciclistaPostDTO.setMeioDePagamento(cartaoDTO); //Faltava @NotNull

        //converte dto valido para json
        ciclistaPostDTOJson = objectMapper.writeValueAsString(ciclistaPostDTO);
    }

    //POST Ciclista

    @Test
    @DisplayName("POST /ciclista - Deve retornar 201 Created em um cadastro com sucesso")
    void deveRetornar201QuandoCadastrarComSucesso() throws Exception {

        when(service.cadastrarCiclista(any(CiclistaPostDTO.class))).thenReturn(ciclistaDTO);

        mockMvc.perform(post("/ciclista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ciclistaPostDTOJson)) //Envia o JSON
                .andExpect(status().isCreated()) //Espera HTTP 201
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("teste.valido@email.com"));
    }

    @Test
    @DisplayName("POST /ciclista - Deve retornar 422 Unprocessable Entity por lógica de negócio (ex: Email duplicado)")
    void deveRetornar422PorFalhaDeLogica() throws Exception {
        String mensagemErro = "Email já cadastrado.";
        //Diz ao serviço falso para lançar a exceção de lógica
        when(service.cadastrarCiclista(any(CiclistaPostDTO.class)))
                .thenThrow(new DadosInvalidosException(mensagemErro));

        mockMvc.perform(post("/ciclista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ciclistaPostDTOJson))
                .andExpect(status().isUnprocessableEntity()) //Espera HTTP 422
                .andExpect(jsonPath("$.codigo").value("422 UNPROCESSABLE_ENTITY"))
                .andExpect(jsonPath("$.mensagem").value(mensagemErro));
    }

    @Test
    @DisplayName("POST /ciclista - Deve retornar 422 por falha de validação (@Valid)")
    void deveRetornar422PorFalhaDeValidacao() throws Exception {

        //DTO inválido (ex: email em branco)
        CiclistaPostDTO dtoInvalido = new CiclistaPostDTO();
        dtoInvalido.setNome("Teste");
        //Email está nulo, vai falhar o @NotBlank

        String jsonInvalido = objectMapper.writeValueAsString(dtoInvalido);

        // ACT & ASSERT
        mockMvc.perform(post("/ciclista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isUnprocessableEntity()) // Espera HTTP 422 (como definimos no Handler)
                .andExpect(jsonPath("$.codigo").value("422 UNPROCESSABLE_ENTITY"));
    }



    //GET EMAIL
    @Test
    @DisplayName("GET /existeEmail - Deve retornar 200 OK com 'true' quando email existe")
    void deveRetornarOkETrueQuandoEmailExiste() throws Exception {
        when(service.existeEmail("email@jaexiste.com")).thenReturn(true);
        mockMvc.perform(get("/ciclista/existeEmail/{email}", "email@jaexiste.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("GET /existeEmail - Deve retornar 200 OK com 'false' quando email não existe")
    void deveRetornarOkEFalseQuandoEmailNaoExiste() throws Exception {
        when(service.existeEmail("email@valido.com")).thenReturn(false);
        mockMvc.perform(get("/ciclista/existeEmail/{email}", "email@valido.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("GET /existeEmail - Deve retornar 422 Unprocessable Entity quando email é inválido")
    void deveRetornar422QuandoEmailInvalido() throws Exception {
        String mensagemErro = "Formato de email inválido.";
        when(service.existeEmail("formatoinvalido"))
                .thenThrow(new DadosInvalidosException(mensagemErro));

        mockMvc.perform(get("/ciclista/existeEmail/{email}", "formatoinvalido"))
                .andExpect(status().isUnprocessableEntity()) // Espera HTTP 422
                .andExpect(jsonPath("$.codigo").value("422 UNPROCESSABLE_ENTITY"))
                .andExpect(jsonPath("$.mensagem").value(mensagemErro));
    }

    //testes ativarCiclista UC02
    @Test
    @DisplayName("POST /ciclista/{id}/ativar - Deve retornar 200 OK ao ativar com sucesso")
    void deveRetornar200AoAtivarComSucesso() throws Exception {
        // ARRANGE
        Long idExistente = 1L;
        CiclistaDTO dtoResposta = new CiclistaDTO();
        dtoResposta.setId(idExistente);
        dtoResposta.setStatus(StatusCiclista.ATIVO);

        when(service.ativarCiclista(idExistente)).thenReturn(dtoResposta);

        // ACT & ASSERT
        mockMvc.perform(post("/ciclista/{id}/ativar", idExistente))
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.id").value(idExistente))
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    @DisplayName("POST /ciclista/{id}/ativar - Deve retornar 404 Not Found para ID inexistente")
    void deveRetornar404AoAtivarIdInexistente() throws Exception {
        // ARRANGE
        Long idInexistente = 99L;
        String msgErro = "Ciclista não encontrado.";

        when(service.ativarCiclista(idInexistente))
                .thenThrow(new RecursoNaoEncontradoException(msgErro));

        // ACT & ASSERT
        mockMvc.perform(post("/ciclista/{id}/ativar", idInexistente))
                .andExpect(status().isNotFound()) // Espera 404
                .andExpect(jsonPath("$.codigo").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.mensagem").value(msgErro));
    }

    @Test
    @DisplayName("POST /ciclista/{id}/ativar - Deve retornar 422 Unprocessable Entity se ciclista não está pendente")
    void deveRetornar422AoAtivarCiclistaNaoPendente() throws Exception {
        // ARRANGE
        Long idExistente = 1L;
        String msgErro = "Este ciclista não está aguardando confirmação.";

        when(service.ativarCiclista(idExistente))
                .thenThrow(new DadosInvalidosException(msgErro));

        // ACT & ASSERT
        mockMvc.perform(post("/ciclista/{id}/ativar", idExistente))
                .andExpect(status().isUnprocessableEntity()) // Espera 422
                .andExpect(jsonPath("$.codigo").value("422 UNPROCESSABLE_ENTITY"))
                .andExpect(jsonPath("$.mensagem").value(msgErro));
    }
}