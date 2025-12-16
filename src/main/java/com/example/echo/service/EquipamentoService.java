package com.example.echo.service;

import com.example.echo.dto.BicicletaDTO;
import com.example.echo.dto.externo.TrancaDTO;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.service.externo.EquipamentoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EquipamentoService {

    @Autowired
    private EquipamentoClient equipamentoClient;

    // A1 e UC01: Verifica se tem bicicleta na tranca e retorna os dados dela
    public BicicletaDTO buscarBicicletaNaTranca(Long idTranca) {
        try {
            // 1. Pergunta para o equipamento o que tem na tranca
            TrancaDTO tranca = equipamentoClient.buscarTranca(idTranca);

            // 2. Se a tranca não existe ou não tem bicicleta presa
            if (tranca == null || tranca.getBicicleta() == null) {
                return null; // Tranca vazia
            }

            // 3. Se tem bicicleta, busca os detalhes dela (para ver se está EM_REPARO, etc)
            return equipamentoClient.buscarBicicleta(tranca.getBicicleta());

        } catch (Exception e) {
            // Se a tranca não for encontrada na API externa (404) ou erro de rede
            System.err.println("Erro ao buscar tranca: " + e.getMessage());
            throw new RecursoNaoEncontradoException("Tranca não encontrada ou serviço indisponível.");
        }
    }

    // UC01: Destranca a bicicleta para o ciclista pegar
    public boolean destrancarTranca(Long idTranca) {
        try {
            // Precisamos saber qual bicicleta está lá para destrancar corretamente
            TrancaDTO tranca = equipamentoClient.buscarTranca(idTranca);

            if (tranca != null && tranca.getBicicleta() != null) {
                // Chama a API externa para liberar a trava
                equipamentoClient.destrancarTranca(idTranca, tranca.getBicicleta());
                return true;
            }
            return false; // Não tem bicicleta para destrancar

        } catch (Exception e) {
            System.err.println("Erro ao destrancar: " + e.getMessage());
            return false;
        }
    }

    // Usado na Devolução para mudar status para DISPONIVEL ou EM_REPARO
    public void alterarStatusBicicleta(Long idBicicleta, String novoStatus) {
        try {
            equipamentoClient.alterarStatusBicicleta(idBicicleta, novoStatus);
        } catch (Exception e) {
            System.err.println("Erro ao alterar status da bike: " + e.getMessage());
            throw new DadosInvalidosException("Erro ao atualizar bicicleta no equipamento.");
        }
    }

    // [IMPORTANTE] Atualizei a assinatura para receber o ID da Bicicleta também
    public void trancarTranca(Long idTranca, Long idBicicleta) {
        try {
            equipamentoClient.trancarTranca(idTranca, idBicicleta);
        } catch (Exception e) {
            System.err.println("Erro ao trancar tranca " + idTranca + ": " + e.getMessage());
            throw new DadosInvalidosException("Falha ao travar a tranca na devolução.");
        }
    }
}