package com.example.echo.validation;


import com.example.echo.dto.CiclistaPostDTO;
import com.example.echo.dto.PassaporteDTO;
import com.example.echo.model.Nacionalidade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

class ValidaNacionalidadeValidatorTest {

    private ValidaNacionalidadeValidator validator;
    private CiclistaPostDTO dto;

    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        //inicializa os mocks
        MockitoAnnotations.openMocks(this);

        validator = new ValidaNacionalidadeValidator();
        dto = new CiclistaPostDTO();


        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
    }

    @Test
    @DisplayName("Deve retornar true para Brasileiro com CPF")
    void deveRetornarTrueParaBrasileiroComCpf() {
        dto.setNacionalidade(Nacionalidade.BRASILEIRO);
        dto.setCpf("12345678901");
        assertTrue(validator.isValid(dto, context));
    }

    @Test
    @DisplayName("Deve retornar false para Brasileiro SEM CPF")
    void deveRetornarFalseParaBrasileiroSemCpf() {
        dto.setNacionalidade(Nacionalidade.BRASILEIRO);
        dto.setCpf(null);
        assertFalse(validator.isValid(dto, context));
    }

    @Test
    @DisplayName("Deve retornar true para Estrangeiro com Passaporte")
    void deveRetornarTrueParaEstrangeiroComPassaporte() {
        dto.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        PassaporteDTO passaporteValido = new PassaporteDTO();
        passaporteValido.setNumero("A1B234567");
        passaporteValido.setPais("Canada");
        passaporteValido.setValidade(LocalDate.now().plusYears(1));
    }

    @Test
    @DisplayName("Deve retornar false para Estrangeiro SEM Passaporte")
    void deveRetornarFalseParaEstrangeiroSemPassaporte() {
        dto.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        dto.setPassaporte(null);
        assertFalse(validator.isValid(dto, context));
    }

    @Test
    @DisplayName("Deve retornar true se o DTO for nulo (outra anotação trata)")
    void deveRetornarTrueSeDtoForNulo() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    @DisplayName("Deve retornar true se a nacionalidade for nula (outra anotação trata)")
    void deveRetornarTrueSeNacionalidadeForNula() {
        dto.setNacionalidade(null);
        assertTrue(validator.isValid(dto, context));
    }
}