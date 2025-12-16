package com.example.echo.service.externo;

import com.example.echo.dto.BicicletaDTO;
import com.example.echo.dto.externo.TrancaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EquipamentoClient {

    private final RestTemplate restTemplate;

    public EquipamentoClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Value("${api.equipamento.url}")
    private String urlBase;

    public BicicletaDTO buscarBicicleta(Long id) {
        String url = urlBase + "/bicicleta/" + id;
        return restTemplate.getForObject(url, BicicletaDTO.class);
    }

    public void alterarStatusBicicleta(Long id, String status) {
        // Exemplo: POST /bicicleta/{id}/status/{status}
        // Verifique no Swagger se a rota é exatamente essa
        String url = urlBase + "/bicicleta/" + id + "/status/" + status;
        restTemplate.postForEntity(url, null, Void.class);
    }

    public TrancaDTO buscarTranca(Long id) {
        String url = urlBase + "/tranca/" + id;
        return restTemplate.getForObject(url, TrancaDTO.class);
    }

    public void trancarTranca(Long idTranca, Long idBicicleta) {
        // Geralmente precisa informar qual bicicleta está sendo trancada
        String url = urlBase + "/tranca/" + idTranca + "/trancar";
        // Se precisar enviar o ID da bike no corpo, crie um DTO ou map.
        // Aqui estou assumindo que pode passar o ID da bike na URL ou corpo se necessário.
        // *VERIFIQUE O SWAGGER DESSA ROTA*

        // Exemplo simples (post vazio):
        restTemplate.postForEntity(url, idBicicleta, Void.class);
    }

    public void destrancarTranca(Long idTranca, Long idBicicleta) {
        String url = urlBase + "/tranca/" + idTranca + "/destrancar";

        // Exemplo enviando ID da bike no corpo
        restTemplate.postForEntity(url, idBicicleta, Void.class);
    }
}