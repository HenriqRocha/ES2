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

    @BeforeEach
    void setup() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        client = new EquipamentoClient(restTemplateBuilder);
    }

    @Test
    @DisplayName("Deve buscar tranca com sucesso")
    void deveBuscarTranca() {
        TrancaDTO mockTranca = new TrancaDTO();
        mockTranca.setId(1L);

        when(restTemplate.getForObject(contains("/tranca/1"), eq(TrancaDTO.class)))
                .thenReturn(mockTranca);

        TrancaDTO resultado = client.buscarTranca(1L);
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("Deve buscar bicicleta com sucesso")
    void deveBuscarBicicleta() {
        BicicletaDTO mockBike = new BicicletaDTO(10L, "DISPONIVEL");

        when(restTemplate.getForObject(contains("/bicicleta/10"), eq(BicicletaDTO.class)))
                .thenReturn(mockBike);

        BicicletaDTO resultado = client.buscarBicicleta(10L);
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
    }

    @Test
    @DisplayName("Deve destrancar tranca com sucesso (Void)")
    void deveDestrancarTranca() {
        // Mock de método void no RestTemplate
        when(restTemplate.postForEntity(contains("/destrancar"), any(), eq(Void.class)))
                .thenReturn(null);

        assertDoesNotThrow(() -> client.destrancarTranca(1L, 10L));
    }

    @Test
    @DisplayName("Deve tratar erro ao buscar tranca (retornar null ou lançar exceção tratada)")
    void deveTratarErroBuscaTranca() {
        when(restTemplate.getForObject(contains("/tranca"), eq(TrancaDTO.class)))
                .thenThrow(new RuntimeException("Erro 404"));

        // Ajuste conforme seu código: se ele retorna null no catch, use assertNull.
        // Se ele relança a exceção, use assertThrows.
        // Assumindo que ele lança RuntimeException como o ExternoClient:
        assertThrows(RuntimeException.class, () -> client.buscarTranca(1L));
    }
}