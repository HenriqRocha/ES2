package com.example.echo.controller;

import com.example.echo.service.AluguelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DadosController {

    @Autowired
    private AluguelService aluguelService;

    // GET /restaurarBanco
    @GetMapping("/restaurarBanco")
    public ResponseEntity<Void> restaurarBanco() {
        aluguelService.restaurarBanco();
        return ResponseEntity.ok().build();
    }
}