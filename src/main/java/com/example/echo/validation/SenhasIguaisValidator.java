package com.example.echo.validation;

import com.example.echo.dto.CiclistaPostDTO;
import com.example.echo.dto.CiclistaPutDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class SenhasIguaisValidator implements ConstraintValidator<SenhasIguais, Object> {

    @Override
    public void initialize(SenhasIguais constraintAnnotation) {

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String senha = null;
        String confirmacaoSenha = null;

        //tenho que verificar qual é o dto
        if (value instanceof CiclistaPostDTO) {
            CiclistaPostDTO dto = (CiclistaPostDTO) value;
            senha = dto.getSenha();
            confirmacaoSenha = dto.getConfirmacaoSenha();
        }
        else if (value instanceof CiclistaPutDTO) {
            CiclistaPutDTO dto = (CiclistaPutDTO) value;
            senha = dto.getSenha();
            confirmacaoSenha = dto.getConfirmacaoSenha();
        }
        else {
            return true;
        }


        //caso para atualização, a senha pode não ter sido alterada
        if (senha == null && confirmacaoSenha == null) {
            return true;
        }

        //Verifica a igualdade
        return Objects.equals(senha, confirmacaoSenha);
    }
}