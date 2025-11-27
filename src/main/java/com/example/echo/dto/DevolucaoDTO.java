package com.example.echo.dto;

import javax.validation.constraints.NotNull;

public class DevolucaoDTO {

    @NotNull(message = "ID do ciclista é obrigatório")
    private Long ciclistaId;

    @NotNull(message = "ID da tranca de devolução é obrigatório")
    private Long trancaFimId;

    //A3 O ciclista seleciona a opção para requisitar reparo
    private boolean defeito = false;

    //Getters e Setters
    public Long getCiclistaId() { return ciclistaId; }
    public void setCiclistaId(Long ciclistaId) { this.ciclistaId = ciclistaId; }
    public Long getTrancaFimId() { return trancaFimId; }
    public void setTrancaFimId(Long trancaFimId) { this.trancaFimId = trancaFimId; }
    public boolean isDefeito() { return defeito; }
    public void setDefeito(boolean defeito) { this.defeito = defeito; }
}