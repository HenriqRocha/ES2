package com.example.echo.controller;

import com.example.echo.dto.AluguelDTO;
import com.example.echo.dto.NovoAluguelDTO;
import com.example.echo.service.AluguelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/aluguel")
public class AluguelController {

    @Autowired
    private AluguelService aluguelService;

    //POST aluguel UC03
    @PostMapping
    public ResponseEntity<AluguelDTO> realizarAluguel(
            @Valid @RequestBody NovoAluguelDTO novoAluguelDTO
    ) {
        AluguelDTO aluguelCriado = aluguelService.realizarAluguel(novoAluguelDTO);

        //200
        return ResponseEntity.status(HttpStatus.OK).body(aluguelCriado);
    }

}