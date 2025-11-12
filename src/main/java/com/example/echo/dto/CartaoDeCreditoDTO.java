package com.example.echo.dto;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

//uso dentro do CiclistaPostDTO
public class CartaoDeCreditoDTO {

    @NotBlank(message = "Nome do titular é obrigatório")
    private String nomeTitular;

    @NotBlank(message = "Número do cartão é obrigatório")
    @Pattern(regexp = "^[0-9]{16}$", message = "Número do cartão deve ter 16 dígitos")
    private String numero;

    @NotNull(message = "Validade do cartão é obrigatória")
    @Future(message = "Cartão de crédito está expirado")
    private LocalDate validade;

    @NotBlank(message = "CVV é obrigatório")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV deve ter 3 ou 4 dígitos")
    private String cvv;

    // Getters e Setters
    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public LocalDate getValidade() { return validade; }
    public void setValidade(LocalDate validade) { this.validade = validade; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}