package com.example.echo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String message) {
        super(message);
    }
}
