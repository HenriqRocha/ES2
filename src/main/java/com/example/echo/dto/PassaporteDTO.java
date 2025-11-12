package com.example.echo.dto;

import java.time.LocalDate;

//usa dentro do CiclistaPostDTO
public class PassaporteDTO {
    private String numero;
    private LocalDate validade;
    private String pais;

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public LocalDate getValidade() { return validade; }
    public void setValidade(LocalDate validade) { this.validade = validade; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
}