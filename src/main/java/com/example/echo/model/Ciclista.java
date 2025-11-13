package com.example.echo.model;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ciclista")
public class Ciclista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private LocalDate nascimento;

    @Column(unique = true)
    private String cpf;

    private String passaporteNumero;
    private LocalDate passaporteValidade;
    private String passaportePais;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Nacionalidade nacionalidade;

    @Column(nullable = false, unique = true)
    private String email;

    private String urlFotoDocumento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCiclista status;

    @Column(nullable = false)
    private String senha;

    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;

    //relaçao ciclista-cartao
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cartao_de_credito_id", referencedColumnName = "id")
    private CartaoDeCredito cartaoDeCredito;

    public Ciclista() {
        //precisa do contrutor vazio para o JPA
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getNascimento() {
        return nascimento;
    }

    public void setNascimento(LocalDate nascimento) {
        this.nascimento = nascimento;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getPassaporteNumero() {
        return passaporteNumero;
    }

    public void setPassaporteNumero(String passaporteNumero) {
        this.passaporteNumero = passaporteNumero;
    }

    public LocalDate getPassaporteValidade() {
        return passaporteValidade;
    }

    public void setPassaporteValidade(LocalDate passaporteValidade) {
        this.passaporteValidade = passaporteValidade;
    }

    public String getPassaportePais() {
        return passaportePais;
    }

    public void setPassaportePais(String passaportePais) {
        this.passaportePais = passaportePais;
    }

    public Nacionalidade getNacionalidade() {
        return nacionalidade;
    }

    public void setNacionalidade(Nacionalidade nacionalidade) {
        this.nacionalidade = nacionalidade;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrlFotoDocumento() {
        return urlFotoDocumento;
    }

    public void setUrlFotoDocumento(String urlFotoDocumento) {
        this.urlFotoDocumento = urlFotoDocumento;
    }

    public StatusCiclista getStatus() {
        return status;
    }

    public void setStatus(StatusCiclista status) {
        this.status = status;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public CartaoDeCredito getCartaoDeCredito() {
        return cartaoDeCredito;
    }

    public void setCartaoDeCredito(CartaoDeCredito cartaoDeCredito) {
        this.cartaoDeCredito = cartaoDeCredito;
    }

    public void setDataConfirmacao(LocalDateTime dataConfirmacao){this.dataConfirmacao = dataConfirmacao;}

    public LocalDateTime getDataConfirmacao(){return dataConfirmacao;}

}
