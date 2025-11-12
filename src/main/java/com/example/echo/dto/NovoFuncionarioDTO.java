package com.example.echo.dto;

import com.example.echo.model.FuncaoFuncionario;

import javax.validation.constraints.*;

public class NovoFuncionarioDTO {

    @NotBlank(message = "Campo obrigatório")
    private String senha;

    @NotBlank(message = "Campo obrigatório")
    private String confirmacaoSenha;

    @NotBlank(message = "Campo obrigatório")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "Campo obrigatório")
    private String nome;

    @NotNull(message = "Campo obrigatório")
    @Positive(message = "Idade deve ser um número positivo")
    private Integer idade;

    @NotNull(message = "Campo obrigatório")
    private FuncaoFuncionario funcao;

    @NotBlank(message = "Campo obrigatório")
    @Pattern(regexp = "^\\d{11}$", message = "CPF deve conter 11 dígitos, somente números")
    private String cpf;

    public NovoFuncionarioDTO(){
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmacaoSenha() {
        return confirmacaoSenha;
    }

    public void setConfirmacaoSenha(String confirmacaoSenha) {
        this.confirmacaoSenha = confirmacaoSenha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public FuncaoFuncionario getFuncao() {
        return funcao;
    }

    public void setFuncao(FuncaoFuncionario funcao) {
        this.funcao = funcao;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}
