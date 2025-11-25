package com.example.echo.validation;

import com.example.echo.dto.CiclistaPostDTO;
import com.example.echo.dto.CiclistaPutDTO; // <--- Importante: Adicionamos o PUT
import com.example.echo.dto.PassaporteDTO;
import com.example.echo.model.Nacionalidade;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

// MUDANÇA 1: Generic type mudou de CiclistaPostDTO para Object
public class ValidaNacionalidadeValidator implements ConstraintValidator<ValidaNacionalidade, Object> {

    @Override
    public void initialize(ValidaNacionalidade constraintAnnotation) {
        // Inicialização padrão
    }

    // MUDANÇA 2: Recebemos Object value
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context){
        if (value == null){
            return true;
        }

        // Variáveis para extrair os dados independente do DTO
        Nacionalidade nacionalidade = null;
        String cpf = null;
        PassaporteDTO passaporte = null;

        // MUDANÇA 3: Verificamos o tipo e fazemos o cast correto
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
            // Se a anotação for usada numa classe que não conhecemos, ignoramos
            return true;
        }

        // --- LÓGICA DE VALIDAÇÃO (Reaproveitando a sua) ---

        // Se a nacionalidade for nula, passamos (importante para o PUT parcial)
        if (nacionalidade == null) {
            return true;
        }

        // R1: Se Brasileiro, CPF obrigatório
        if (nacionalidade == Nacionalidade.BRASILEIRO && (cpf == null || cpf.trim().isEmpty())){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("CPF é obrigatório para brasileiros")
                    .addPropertyNode("cpf").addConstraintViolation();
            return false;
        }

        // R1: Se Estrangeiro, Passaporte obrigatório
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