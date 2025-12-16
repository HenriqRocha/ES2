package com.example.echo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_aluguel")
public class Aluguel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //relacionamento aluguel-ciclista
    @ManyToOne
    @JoinColumn(name = "ciclista_id", nullable = false)
    private Ciclista ciclista;

    //equipamento
    @Column(nullable = false)
    private Long bicicleta;

    @Column(nullable = false)
    private Long trancaInicio;

    private Long trancaFim;

    //externo
    @Column
    private Long cobranca;

    @Column(nullable = false)
    private LocalDateTime horaInicio;

    private LocalDateTime horaFim;

    @Column
    private Double valorExtra = 0.0;

    public Aluguel() {
        //precisa do contrutor vazio para o JPA
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ciclista getCiclista() {
        return ciclista;
    }

    public void setCiclista(Ciclista ciclista) {
        this.ciclista = ciclista;
    }

    public Long getBicicleta() {
        return bicicleta;
    }

    public void setBicicleta(Long bicicleta) {
        this.bicicleta = bicicleta;
    }

    public Long getTrancaInicio() {
        return trancaInicio;
    }

    public void setTrancaInicio(Long trancaInicio) {
        this.trancaInicio = trancaInicio;
    }

    public Long getTrancaFim() {
        return trancaFim;
    }

    public void setTrancaFim(Long trancaFim) {
        this.trancaFim = trancaFim;
    }

    public Long getCobranca() {
        return cobranca;
    }

    public void setCobranca(Long cobranca) {
        this.cobranca = cobranca;
    }

    public LocalDateTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalDateTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalDateTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalDateTime horaFim) {
        this.horaFim = horaFim;
    }

    public Double getValorExtra() { return valorExtra; }
    public void setValorExtra(Double valorExtra) { this.valorExtra = valorExtra; }
}
