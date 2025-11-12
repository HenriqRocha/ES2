package com.example.echo.dto;

import com.example.echo.model.Nacionalidade;
import com.example.echo.validation.SenhasIguais;
import com.example.echo.validation.ValidaNacionalidade;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@SenhasIguais
@ValidaNacionalidade
public class CiclistaPostDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull(message = "Nascimento é obrigatório")
    private LocalDate nascimento;

    //R1 cuida do resto
    @Pattern(regexp = "^[0-9]{11}$", message = "CPF deve ter 11 dígitos")
    private String cpf;

    @Valid
    private PassaporteDTO passaporte;

    @NotNull(message = "Nacionalidade é obrigatória")
    private Nacionalidade nacionalidade;

    //login
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    @NotBlank(message = "Senha é obrigatória")
    private String confirmacaoSenha;

    //cartao
    @NotNull(message = "Meio de pagamento é obrigatório")
    @Valid
    private CartaoDeCreditoDTO meioDePagamento;

    //Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getNascimento() { return nascimento; }
    public void setNascimento(LocalDate nascimento) { this.nascimento = nascimento; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public PassaporteDTO getPassaporte() { return passaporte; }
    public void setPassaporte(PassaporteDTO passaporte) { this.passaporte = passaporte; }
    public Nacionalidade getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(Nacionalidade nacionalidade) { this.nacionalidade = nacionalidade; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getConfirmacaoSenha() { return confirmacaoSenha; }
    public void setConfirmacaoSenha(String confirmacaoSenha) { this.confirmacaoSenha = confirmacaoSenha; }
    public CartaoDeCreditoDTO getMeioDePagamento() { return meioDePagamento; }
    public void setMeioDePagamento(CartaoDeCreditoDTO meioDePagamento) { this.meioDePagamento = meioDePagamento; }


}
