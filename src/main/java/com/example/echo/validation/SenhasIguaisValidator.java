package com.example.echo.validation;

import com.example.echo.dto.CiclistaPostDTO;
import com.example.echo.dto.CiclistaPutDTO; // <--- Importante: Adicione este import

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

// MUDANÇA 1: Trocamos CiclistaPostDTO por Object
public class SenhasIguaisValidator implements ConstraintValidator<SenhasIguais, Object> {

    @Override
    public void initialize(SenhasIguais constraintAnnotation) {
        // Método vazio
    }

    // MUDANÇA 2: O parâmetro agora é Object
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String senha = null;
        String confirmacaoSenha = null;

        // MUDANÇA 3: Verificamos qual DTO é e fazemos o cast
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
            // Se usarem a anotação em uma classe desconhecida, ignoramos (ou retornamos false)
            return true;
        }

        // Validação Inteligente:

        // Caso Especial para PUT: Se o usuário NÃO enviou senhas (ambas nulas), é válido.
        if (senha == null && confirmacaoSenha == null) {
            return true;
        }

        // Verifica a igualdade (Objects.equals já trata se um for null e o outro não)
        return Objects.equals(senha, confirmacaoSenha);
    }
}