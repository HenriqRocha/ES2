package com.example.echo.dto;

import com.example.echo.model.FuncaoFuncionario;
import com.example.echo.model.Funcionario;
public class FuncionarioDTO {
    private Long matricula;

    private String email;
    private String nome;
    private int idade;
    private FuncaoFuncionario funcao;
    private String cpf;

    public FuncionarioDTO() {
    }

   //mapper
    public FuncionarioDTO(Funcionario funcionario) {
        this.matricula = funcionario.getId();
        this.email = funcionario.getEmail();
        this.nome = funcionario.getNome();
        this.idade = funcionario.getIdade();
        this.funcao = funcionario.getFuncao();
        this.cpf = funcionario.getCpf();
    }

    public Long getMatricula() {
        return matricula;
    }

    public void setMatricula(Long matricula) {
        this.matricula = matricula;
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

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
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
