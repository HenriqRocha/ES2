package com.example.echo.service;

import com.example.echo.dto.BicicletaDTO; // Certifique-se de ter criado este DTO simples
import org.springframework.stereotype.Service;

@Service
public class EquipamentoService {

    // Simula GET /tranca/{id}/bicicleta
    public BicicletaDTO buscarBicicletaNaTranca(Long idTranca) {
        // Simulação de cenários de erro para testes
        if (idTranca == 999L) {
            return null; // [E2] Simula tranca vazia
        }
        if (idTranca == 888L) {
            // [E4] Simula bicicleta com defeito
            return new BicicletaDTO(200L, "EM_REPARO");
        }

        // Cenário de Sucesso
        return new BicicletaDTO(100L, "DISPONIVEL");
    }

    // Simula POST /tranca/{id}/destrancar
    public boolean destrancarTranca(Long idTranca) {
        // [E5] Simula que a tranca 777 está emperrada
        if (idTranca == 777L) {
            return false;
        }
        return true; // Sucesso
    }

    public void alterarStatusBicicleta(Long idBicicleta, String novoStatus) {
        System.out.println("MOCK: Bicicleta " + idBicicleta + " status alterado para " + novoStatus);
    }

    // Simula POST /tranca/{id}/trancar
    public void trancarTranca(Long idTranca) {
        System.out.println("MOCK: Tranca " + idTranca + " fechada (ocupada).");
    }
}