package com.example.echo.validation;

import com.example.echo.dto.CiclistaPostDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class SenhasIguaisValidator implements ConstraintValidator<SenhasIguais, CiclistaPostDTO> {
    @Override
    public void initialize(SenhasIguais contraintAnnotation){
        //Método vazio
    }

    //validação nova R2
    @Override
    public boolean isValid(CiclistaPostDTO dto, ConstraintValidatorContext context){
        if (dto == null){//não verifico aqui se é nulo então pode passar
            return true;
        }

        //pega as duas senhas
        String senha = dto.getSenha();
        String confirmacaoSenha = dto.getConfirmacaoSenha();

        return Objects.equals(senha, confirmacaoSenha);//são iguais ou não
    }
}
