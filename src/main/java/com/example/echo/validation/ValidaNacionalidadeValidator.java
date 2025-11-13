package com.example.echo.validation;

import com.example.echo.dto.CiclistaPostDTO;
import com.example.echo.model.Nacionalidade;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidaNacionalidadeValidator implements ConstraintValidator<ValidaNacionalidade, CiclistaPostDTO> {
    @Override
    public boolean isValid(CiclistaPostDTO dto, ConstraintValidatorContext context){
        //o @Notnull pega se for nulo
        if (dto == null || dto.getNacionalidade() == null){return true;}

        Nacionalidade nacionalidade = dto.getNacionalidade();

        //R1 if brasileiro cpf obrigatorio
        if (nacionalidade == Nacionalidade.BRASILEIRO && (dto.getCpf() == null || dto.getCpf().trim().isEmpty())){

            //criando mensagem especifica de cpf
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("CPF é obrigatório para brasileiros")
                    .addPropertyNode("cpf").addConstraintViolation();
            return false;
        }

        //R1 if estrangeiro passaporte obrigatório
        if (nacionalidade == Nacionalidade.ESTRANGEIRO && (dto.getPassaporte() == null || dto.getPassaporte().getNumero() == null ||
                dto.getPassaporte().getPais() == null || dto.getPassaporte().getValidade() == null)){

            //mensagem específica de passaporte
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Dados do passaporte são obrigatórios para estrangeiros")
                    .addPropertyNode("passaporte").addConstraintViolation();
            return false;

        }
        return true;
    }
}
