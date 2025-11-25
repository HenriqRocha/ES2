package com.example.echo.dto;

import com.example.echo.model.Aluguel;
import java.time.LocalDateTime;
public class AluguelDTO {
    private Long id;
    private Long ciclistaId;
    private Long bicicletaId;
    private Long trancaInicioId;
    private Long trancaFimId;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private Long cobrancaId;

    public AluguelDTO(Aluguel aluguel){
        this.id = aluguel.getId();
        this.ciclistaId = aluguel.getCiclista().getId();
        this.bicicletaId = aluguel.getBicicleta();
        this.trancaInicioId = aluguel.getTrancaInicio();
        this.trancaFimId = aluguel.getTrancaFim();
        this.dataHoraInicio = aluguel.getHoraInicio();
        this.dataHoraFim = aluguel.getHoraFim();
        this.cobrancaId = aluguel.getCobranca();
    }

    //Getters e Setters
    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}

    public Long getCiclistaId(){return ciclistaId;}
    public void setCiclistaId(Long ciclistaId){this.ciclistaId = ciclistaId;}

    public Long getBicicletaId(){return bicicletaId;}
    public void setBicicletaId(Long bicicletaId){this.bicicletaId = bicicletaId;}

    public Long getTrancaInicioId(){return trancaInicioId;}
    public void setTrancaInicioId(Long trancaInicioId){this.trancaInicioId = trancaInicioId;}

    public Long getTrancaFimId(){return trancaFimId;}
    public void setTrancaFimId(Long trancaFimId){this.trancaFimId = trancaFimId;}

    public Long getCobrancaId(){return cobrancaId;}
    public void setCobrancaId(Long cobrancaId){this.cobrancaId = cobrancaId;}

    public LocalDateTime getDataHoraInicio(){return dataHoraInicio;}
    public void setDataHoraInicio(LocalDateTime dataHoraInicio){this.dataHoraInicio = dataHoraInicio;}

    public LocalDateTime getDataHoraFim(){return dataHoraFim;}
    public void setDataHoraFim(LocalDateTime dataHoraFim){this.dataHoraFim = dataHoraFim;}

}
