package com.example.echo.service;

import org.springframework.stereotype.Service;

@Service
public class CobrancaService {

    // Simula a cobrança no cartão
    public boolean autorizarPagamento(String numeroCartao, Double valor) {
        // [E3] Simula falha se o cartão começar com "0000"
        if (numeroCartao != null && numeroCartao.startsWith("0000")) {
            return false;
        }
        return true; // Pagamento autorizado
    }

    // Simula o registro de dívida [E3.2]
    public void registrarCobrancaPendente(Long ciclistaId, Double valor) {
        System.out.println("Cobrança pendente registrada para o ciclista: " + ciclistaId + " Valor: " + valor);
    }
}