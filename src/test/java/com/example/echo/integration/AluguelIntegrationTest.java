package com.example.echo.integration;

import com.example.echo.dto.BicicletaDTO;
import com.example.echo.dto.externo.CobrancaDTO;
import com.example.echo.dto.DevolucaoDTO;
import com.example.echo.dto.NovoAluguelDTO;
import com.example.echo.dto.externo.CartaoExternoDTO;
import com.example.echo.dto.externo.TrancaDTO;
import com.example.echo.model.Ciclista;
import com.example.echo.model.Nacionalidade;
import com.example.echo.model.StatusCiclista;
import com.example.echo.repository.AluguelRepository;
import com.example.echo.repository.CiclistaRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class AluguelIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CiclistaRepository ciclistaRepository;

    @Autowired
    private AluguelRepository aluguelRepository;

    @MockBean
    private ExternoClient externoClient;

    @MockBean
    private EquipamentoClient equipamentoClient;

    private Ciclista ciclistaSalvo;

    @BeforeEach
    void setup() {

        aluguelRepository.deleteAll();
        ciclistaRepository.deleteAll();


        Ciclista c = new Ciclista();
        c.setNome("Ciclista Integração");
        c.setEmail("integra@teste.com");
        c.setStatus(StatusCiclista.ATIVO);

        c.setNascimento(java.time.LocalDate.of(1990, 1, 1));
        c.setNacionalidade(Nacionalidade.BRASILEIRO);
        c.setCpf("11122233344");
        c.setSenha("senha123");

        ciclistaSalvo = ciclistaRepository.save(c);
    }

    @Test
    @DisplayName("INTEGRAÇÃO: Deve realizar o fluxo completo de aluguel")
    void deveRealizarAluguelCompleto() throws Exception {
        // 1. Preparar DTO de entrada
        NovoAluguelDTO pedido = new NovoAluguelDTO();
        pedido.setCiclistaId(ciclistaSalvo.getId());
        pedido.setTrancaInicioId(10L);

        // 2. Mockar respostas dos clientes externos (Simular o mundo real)

        // Simula tranca com bicicleta
        TrancaDTO trancaMock = new TrancaDTO();
        trancaMock.setId(10L);
        trancaMock.setBicicleta(200L); // Tem a bike 200
        when(equipamentoClient.buscarTranca(10L)).thenReturn(trancaMock);

        // Simula bicicleta disponível
        BicicletaDTO bikeMock = new BicicletaDTO(200L, "DISPONIVEL");
        when(equipamentoClient.buscarBicicleta(200L)).thenReturn(bikeMock);

        // Simula cobrança autorizada
        CobrancaDTO cobrancaOk = new CobrancaDTO();
        cobrancaOk.setStatus("PAGA");
        when(externoClient.realizarCobranca(any(), anyLong())).thenReturn(cobrancaOk);

        // Simula destrancamento ok
        doNothing().when(equipamentoClient).destrancarTranca(anyLong(), anyLong());

        // 3. Executar a requisição HTTP POST /aluguel
        mockMvc.perform(post("/aluguel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.bicicletaId").value(200L))
                .andExpect(jsonPath("$.trancaInicioId").value(10L));
    }

    @Test
    @DisplayName("INTEGRAÇÃO: Deve falhar aluguel se ciclista não existe")
    void deveFalharAluguelCiclistaInexistente() throws Exception {
        NovoAluguelDTO pedido = new NovoAluguelDTO();
        pedido.setCiclistaId(9999L); // ID que não existe
        pedido.setTrancaInicioId(10L);

        mockMvc.perform(post("/aluguel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isNotFound()); // Espera 404
    }
}