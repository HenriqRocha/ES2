package com.example.echo.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidaNacionalidadeValidator.class)
public @interface ValidaNacionalidade {

    String message() default "Dados de nacionalidade inválidos";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
