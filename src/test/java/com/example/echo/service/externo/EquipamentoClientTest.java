package com.example.echo.service.externo;

import com.example.echo.dto.BicicletaDTO;
import com.example.echo.dto.externo.TrancaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipamentoClientTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private EquipamentoClient client;

    // URL fake para o teste não quebrar na concatenação de strings
    private final String URL_BASE = "http://fake-api.com";

    @BeforeEach
    void setup() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        client = new EquipamentoClient(restTemplateBuilder);

        // Injeta o valor da URL (simula o @Value do Spring)
        ReflectionTestUtils.setField(client, "urlBase", URL_BASE);
    }

    @Test
    @DisplayName("Deve buscar tranca com sucesso")
    void deveBuscarTranca() {
        TrancaDTO mockTranca = new TrancaDTO();
        mockTranca.setId(1L);

        // Usamos eq com a URL completa para garantir que a concatenação está certa
        when(restTemplate.getForObject(eq(URL_BASE + "/tranca/1"), eq(TrancaDTO.class)))
                .thenReturn(mockTranca);

        TrancaDTO resultado = client.buscarTranca(1L);
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("Deve buscar bicicleta com sucesso")
    void deveBuscarBicicleta() {
        BicicletaDTO mockBike = new BicicletaDTO(10L, "DISPONIVEL");

        when(restTemplate.getForObject(eq(URL_BASE + "/bicicleta/10"), eq(BicicletaDTO.class)))
                .thenReturn(mockBike);

        BicicletaDTO resultado = client.buscarBicicleta(10L);
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
    }

    @Test
    @DisplayName("Deve destrancar tranca com sucesso")
    void deveDestrancarTranca() {
        // Verifica se a chamada POST foi feita para a URL correta com o corpo correto (ID da bike)
        client.destrancarTranca(1L, 10L);

        verify(restTemplate).postForEntity(
                eq(URL_BASE + "/tranca/1/destrancar"),
                eq(10L), // Corpo da requisição
                eq(Void.class)
        );
    }

    @Test
    @DisplayName("Deve trancar tranca com sucesso")
    void deveTrancarTranca() {
        // Esse era um dos métodos que faltava testar
        client.trancarTranca(5L, 20L);

        verify(restTemplate).postForEntity(
                eq(URL_BASE + "/tranca/5/trancar"),
                eq(20L), // Corpo da requisição
                eq(Void.class)
        );
    }

    @Test
    @DisplayName("Deve alterar status da bicicleta")
    void deveAlterarStatusBicicleta() {
        // Esse também faltava
        client.alterarStatusBicicleta(50L, "EM_USO");

        verify(restTemplate).postForEntity(
                eq(URL_BASE + "/bicicleta/50/status/EM_USO"),
                isNull(), // Corpo nulo nesse caso
                eq(Void.class)
        );
    }

    @Test
    @DisplayName("Deve lançar erro (RuntimeException) se a API falhar")
    void deveLancarErroNaFalhaDaApi() {
        when(restTemplate.getForObject(anyString(), eq(TrancaDTO.class)))
                .thenThrow(new RuntimeException("API Fora do Ar"));

        assertThrows(RuntimeException.class, () -> client.buscarTranca(1L));
    }
}