package com.example.echo.dto.externo;
public class EmailDTO {
    private String email;
    private String assunto;
    private String mensagem;

    public EmailDTO(String email, String assunto, String mensagem) {
        this.email = email;
        this.assunto = assunto;
        this.mensagem = mensagem;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}