package com.example.echo.exception;

import com.example.echo.dto.ErroDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalHandlerException {

    //404
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroDTO> handleRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex,
            WebRequest request
    ) {

        ErroDTO erro = new ErroDTO(
                HttpStatus.NOT_FOUND.toString(),
                ex.getMessage()
        );

        return new ResponseEntity<>(erro, HttpStatus.NOT_FOUND);
    }

    //400 dto invalido
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        String mensagemDeErro = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        ErroDTO erro = new ErroDTO(
                HttpStatus.UNPROCESSABLE_ENTITY.toString(),
                mensagemDeErro
        );

        return new ResponseEntity<>(erro, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    //442 Unprocessable Entity
    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroDTO> handleDadosInvalidos(
            DadosInvalidosException ex, WebRequest request){
        ErroDTO erro = new ErroDTO(
                HttpStatus.UNPROCESSABLE_ENTITY.toString(), ex.getMessage());
        return new ResponseEntity<>(erro, HttpStatus.UNPROCESSABLE_ENTITY);
        }


}
