package com.example.echo.validation;

import com.example.echo.dto.CiclistaPostDTO;
import com.example.echo.dto.CiclistaPutDTO;
import com.example.echo.dto.PassaporteDTO;
import com.example.echo.model.Nacionalidade;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidaNacionalidadeValidator implements ConstraintValidator<ValidaNacionalidade, Object> {

    @Override
    public void initialize(ValidaNacionalidade constraintAnnotation) {

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context){
        if (value == null){
            return true;
        }

        Nacionalidade nacionalidade = null;
        String cpf = null;
        PassaporteDTO passaporte = null;

        // Verifica o tipo de objeto
        if (value instanceof CiclistaPostDTO) {
            CiclistaPostDTO dto = (CiclistaPostDTO) value;
            nacionalidade = dto.getNacionalidade();
            cpf = dto.getCpf();
            passaporte = dto.getPassaporte();
        }
        else if (value instanceof CiclistaPutDTO) {
            CiclistaPutDTO dto = (CiclistaPutDTO) value;
            nacionalidade = dto.getNacionalidade();
            cpf = dto.getCpf();
            passaporte = dto.getPassaporte();
        }
        else {
            return true;
        }


        //atualização
        if (nacionalidade == null) {
            return true;
        }

        // R1
        if (nacionalidade == Nacionalidade.BRASILEIRO && (cpf == null || cpf.trim().isEmpty())){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("CPF é obrigatório para brasileiros")
                    .addPropertyNode("cpf").addConstraintViolation();
            return false;
        }

        // R1
        if (nacionalidade == Nacionalidade.ESTRANGEIRO &&
                (passaporte == null ||
                        passaporte.getNumero() == null ||
                        passaporte.getPais() == null ||
                        passaporte.getValidade() == null)){

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Dados do passaporte são obrigatórios para estrangeiros")
                    .addPropertyNode("passaporte").addConstraintViolation();
            return false;
        }

        return true;
    }
}