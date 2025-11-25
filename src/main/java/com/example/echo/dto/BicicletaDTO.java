package com.example.echo.dto;

public class BicicletaDTO {
    private Long id;
    private String status; // "DISPONIVEL", "EM_REPARO", "APOSENTADA"

    // Construtores, Getters e Setters
    public BicicletaDTO(Long id, String status) {
        this.id = id;
        this.status = status;
    }
    public Long getId() { return id; }
    public String getStatus() { return status; }
}