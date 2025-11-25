package com.example.echo.dto;

import com.example.echo.model.Nacionalidade;
import com.example.echo.validation.SenhasIguais;
import com.example.echo.validation.ValidaNacionalidade;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

@SenhasIguais
@ValidaNacionalidade
@JsonIgnoreProperties(ignoreUnknown = true)
public class CiclistaPutDTO {

    private String nome;

    @Pattern(regexp = "^[0-9]{11}$", message = "CPF deve ter 11 dígitos")
    private String cpf;

    @Valid
    private PassaporteDTO passaporte;

    private Nacionalidade nacionalidade;

    private String senha;

    private String confirmacaoSenha;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public PassaporteDTO getPassaporte() { return passaporte; }
    public void setPassaporte(PassaporteDTO passaporte) { this.passaporte = passaporte; }
    public Nacionalidade getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(Nacionalidade nacionalidade) { this.nacionalidade = nacionalidade; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getConfirmacaoSenha() { return confirmacaoSenha; }
    public void setConfirmacaoSenha(String confirmacaoSenha) { this.confirmacaoSenha = confirmacaoSenha; }
}
