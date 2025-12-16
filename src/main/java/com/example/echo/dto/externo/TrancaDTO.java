package com.example.echo.dto.externo;

public class TrancaDTO {
    private Long id;
    private Long bicicleta;
    private Long numero;
    private String localizacao;
    private String anoDeFabricacao;
    private String modelo;
    private String status;

    public TrancaDTO() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBicicleta() { return bicicleta; }
    public void setBicicleta(Long bicicleta) { this.bicicleta = bicicleta; }

    public Long getNumero() { return numero; }
    public void setNumero(Long numero) { this.numero = numero; }

    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }

    public String getAnoDeFabricacao() { return anoDeFabricacao; }
    public void setAnoDeFabricacao(String anoDeFabricacao) { this.anoDeFabricacao = anoDeFabricacao; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}