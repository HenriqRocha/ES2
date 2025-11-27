package com.example.echo.service;

import com.example.echo.dto.BicicletaDTO;
import org.springframework.stereotype.Service;

@Service
public class EquipamentoService {

    public BicicletaDTO buscarBicicletaNaTranca(Long idTranca) {
        if (idTranca == 999L) {
            return null; //E2 Simula tranca vazia
        }
        if (idTranca == 888L) {
            //E4 Simula bicicleta com defeito
            return new BicicletaDTO(200L, "EM_REPARO");
        }

        //Sucesso
        return new BicicletaDTO(100L, "DISPONIVEL");
    }

    public boolean destrancarTranca(Long idTranca) {
        //E5 Simula que a tranca 777 está emperrada
        if (idTranca == 777L) {
            return false;
        }
        return true; //Sucesso
    }

    public void alterarStatusBicicleta(Long idBicicleta, String novoStatus) {
        System.out.println("MOCK: Bicicleta " + idBicicleta + " status alterado para " + novoStatus);
    }

    public void trancarTranca(Long idTranca) {
        System.out.println("MOCK: Tranca " + idTranca + " fechada (ocupada).");
    }
}