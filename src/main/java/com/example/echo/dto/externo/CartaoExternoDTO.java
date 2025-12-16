package com.example.echo.dto.externo;

public class CartaoExternoDTO {
    private String nomeTitular;
    private String numero;
    private String validade;
    private String cvv;

    public CartaoExternoDTO(String nomeTitular, String numero, String validade, String cvv) {
        this.nomeTitular = nomeTitular;
        this.numero = numero;
        this.validade = validade;
        this.cvv = cvv;
    }

    public String getNomeTitular() { return nomeTitular; }
    public String getNumero() { return numero; }
    public String getValidade() { return validade; }
    public String getCvv() { return cvv; }
}