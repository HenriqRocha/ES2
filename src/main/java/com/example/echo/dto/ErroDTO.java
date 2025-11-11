package com.example.echo.dto;

public class ErroDTO {
    private String codigo;
    private String mensagem;

    public ErroDTO(String codigo, String mensagem) {
        this.codigo = codigo;
        this.mensagem = mensagem;
    }


    public String getCodigo() {
        return codigo;
    }

    public String getMensagem() {
        return mensagem;
    }
}
