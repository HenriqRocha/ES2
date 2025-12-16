package com.example.echo.service.externo;

import com.example.echo.dto.externo.CartaoExternoDTO;
import com.example.echo.dto.externo.CobrancaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternoClientTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private ExternoClient client;

    @BeforeEach
    void setup() {
        // Simula o comportamento do Builder para retornar nosso Mock de RestTemplate
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        client = new ExternoClient(restTemplateBuilder);
    }

    // --- TESTES DE COBRANÇA ---

    @Test
    @DisplayName("Deve realizar cobrança com sucesso")
    void deveRealizarCobranca() {
        CobrancaDTO respostaMock = new CobrancaDTO();
        respostaMock.setStatus("PAGA");

        when(restTemplate.postForObject(anyString(), any(), eq(CobrancaDTO.class)))
                .thenReturn(respostaMock);

        CobrancaDTO resultado = client.realizarCobranca(10.0, 1L);

        assertNotNull(resultado);
        assertEquals("PAGA", resultado.getStatus());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao falhar cobrança (Caminho do Catch)")
    void deveTratarErroNaCobranca() {
        when(restTemplate.postForObject(anyString(), any(), eq(CobrancaDTO.class)))
                .thenThrow(new RuntimeException("API Fora do Ar"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                client.realizarCobranca(10.0, 1L));

        assertTrue(ex.getMessage().contains("Erro ao processar cobrança"));
    }

    // --- TESTES DE CARTÃO (Aqui estão os Try-Catches complexos) ---

    @Test
    @DisplayName("Deve validar cartão com sucesso (True)")
    void deveValidarCartao() {
        // postForEntity retorna ResponseEntity, mas o método ignora o retorno se não der erro
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(null);

        boolean valido = client.validarCartao(new CartaoExternoDTO("Teste", "1234", "10/2030", "123"));
        assertTrue(valido);
    }

    @Test
    @DisplayName("Deve retornar FALSE quando der erro 422 (Dados Inválidos)")
    void deveRetornarFalsePara422() {
        // Simula o erro específico que o seu try-catch espera
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY));

        boolean valido = client.validarCartao(new CartaoExternoDTO("Teste", "1234", "10/2030", "123"));

        assertFalse(valido); // Deve cair no primeiro catch e retornar false
    }

    @Test
    @DisplayName("Deve retornar FALSE para outros erros genéricos (500, etc)")
    void deveRetornarFalseParaErroGenerico() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("Erro desconhecido"));

        boolean valido = client.validarCartao(new CartaoExternoDTO("Teste", "1234", "10/2030", "123"));

        assertFalse(valido); // Deve cair no segundo catch e retornar false
    }

    // --- TESTES DE FILA E EMAIL (Void methods com logs) ---

    @Test
    @DisplayName("Não deve lançar exceção se falhar envio para fila (Log only)")
    void deveLogarErroFila() {
        when(restTemplate.postForEntity(contains("filaCobranca"), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("Fila cheia"));

        // Não deve explodir erro, apenas logar
        assertDoesNotThrow(() -> client.adicionarCobrancaNaFila(new CobrancaDTO()));
    }

    @Test
    @DisplayName("Não deve lançar exceção se falhar envio de email (Log only)")
    void deveLogarErroEmail() {
        when(restTemplate.postForEntity(contains("enviarEmail"), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("SMTP Error"));

        // Não deve explodir erro, apenas logar
        assertDoesNotThrow(() -> client.enviarEmail("a@a.com", "Assunto", "Msg"));
    }
}