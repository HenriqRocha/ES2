package com.example.echo.dto;

import com.example.echo.model.Nacionalidade;
import com.example.echo.model.StatusCiclista;

import java.time.LocalDate;

public class CiclistaDTO {
    private Long id;
    private String nome;
    private LocalDate nascimento;
    private Nacionalidade nacionalidade;
    private String cpf; // Pode ser nulo se estrangeiro
    private String passaporte; // Pode ser nulo se brasileiro
    private String email;
    private StatusCiclista status;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getNascimento() { return nascimento; }
    public void setNascimento(LocalDate nascimento) { this.nascimento = nascimento; }
    public Nacionalidade getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(Nacionalidade nacionalidade) { this.nacionalidade = nacionalidade; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getPassaporte() { return passaporte; }
    public void setPassaporte(String passaporte) { this.passaporte = passaporte; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public StatusCiclista getStatus() { return status; }
    public void setStatus(StatusCiclista status) { this.status = status; }
}
