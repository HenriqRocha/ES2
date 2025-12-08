package com.example.echo.controller;

import com.example.echo.dto.AluguelDTO;
import com.example.echo.dto.DevolucaoDTO;
import com.example.echo.service.AluguelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/devolucao")
public class DevolucaoController {

    @Autowired
    private AluguelService service;

    //POST aluguel/devolucao UC04
    @PostMapping()
    public ResponseEntity<AluguelDTO> realizarDevolucao(
            @Valid @RequestBody DevolucaoDTO devolucaoDTO
    ) {
        AluguelDTO aluguelFinalizado = service.realizarDevolucao(devolucaoDTO);
        return ResponseEntity.ok(aluguelFinalizado);
    }
}
