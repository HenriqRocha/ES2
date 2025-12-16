package com.example.echo.service.externo;

import com.example.echo.dto.externo.CartaoExternoDTO;
import com.example.echo.dto.externo.CobrancaDTO;
import com.example.echo.dto.externo.EmailDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternoClient {

    private final RestTemplate restTemplate;

    @Value("${api.externo.url}")
    private String urlBase;

    public ExternoClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public CobrancaDTO realizarCobranca(Double valor, Long ciclisttaId) {
        String url = urlBase + "/cobranca";

        // Monta o objeto para enviar
        CobrancaDTO requisicao = new CobrancaDTO();
        requisicao.setValor(valor);
        requisicao.setCiclista(ciclisttaId);

        try {
            // Faz o POST e recebe a resposta
            return restTemplate.postForObject(url, requisicao, CobrancaDTO.class);
        } catch (Exception e) {
            // Se der erro (ex: 422, 500), você trata aqui
            throw new RuntimeException("Erro ao processar cobrança: " + e.getMessage());
        }
    }

    public void adicionarCobrancaNaFila(CobrancaDTO cobranca) {
        String url = urlBase + "/filaCobranca";

        try {
            restTemplate.postForEntity(url, cobranca, Void.class);
            System.out.println("Cobrança enviada para a fila com sucesso.");
        } catch (Exception e) {
            System.err.println("CRÍTICO: Não foi possível nem cobrar nem enfileirar. " + e.getMessage());
        }
    }

    public void enviarEmail(String destinatario, String assunto, String mensagem) {
        String url = urlBase + "/enviarEmail";

        EmailDTO dto = new EmailDTO(destinatario, assunto, mensagem);

        try {
            restTemplate.postForEntity(url, dto, Void.class);
        } catch (Exception e) {
            System.err.println("Erro ao enviar email no serviço externo: " + e.getMessage());
        }
    }

    public boolean validarCartao(CartaoExternoDTO cartaoDto) {
        String url = urlBase + "/validaCartaoDeCredito";

        try {
            restTemplate.postForEntity(url, cartaoDto, Void.class);

            return true;

        } catch (HttpClientErrorException.UnprocessableEntity e) {
            // Captura EXATAMENTE o erro 422 (Dados Inválidos) que a API retorna
            System.err.println("Validação falhou: Cartão rejeitado pela operadora.");
            return false;

        } catch (Exception e) {
            System.err.println("Erro técnico na validação do cartão: " + e.getMessage());
            return false;
        }
    }

}
