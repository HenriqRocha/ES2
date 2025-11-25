package com.example.echo.dto;

import javax.validation.constraints.NotNull;

public class NovoAluguelDTO {

    @NotNull(message = "ID do ciclista é obrigatório")
    private Long ciclistaId;

    @NotNull(message = "ID da tranca de início é obrigatório")
    private Long trancaInicioId;


    //Getters e Setters
    public Long getCiclistaId() { return ciclistaId; }
    public void setCiclistaId(Long ciclistaId) { this.ciclistaId = ciclistaId; }
    public Long getTrancaInicioId() { return trancaInicioId; }
    public void setTrancaInicioId(Long trancaInicioId) { this.trancaInicioId = trancaInicioId; }
}
