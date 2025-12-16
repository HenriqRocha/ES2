package com.example.echo.dto.externo;
import java.time.LocalDateTime;

public class CobrancaDTO {

    // REQUEST
    private Double valor;
    private Long ciclista;

    //RESPOSTA
    private Long id;
    private String status;
    private LocalDateTime horaSolicitacao;
    private LocalDateTime horaFinalizacao;

    public CobrancaDTO() {
    }


    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Long getCiclista() {
        return ciclista;
    }

    public void setCiclista(Long ciclista) {
        this.ciclista = ciclista;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getHoraSolicitacao() {
        return horaSolicitacao;
    }

    public void setHoraSolicitacao(LocalDateTime horaSolicitacao) {
        this.horaSolicitacao = horaSolicitacao;
    }

    public LocalDateTime getHoraFinalizacao() {
        return horaFinalizacao;
    }

    public void setHoraFinalizacao(LocalDateTime horaFinalizacao) {
        this.horaFinalizacao = horaFinalizacao;
    }
}