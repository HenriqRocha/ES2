package com.example.echo.controller;

import com.example.echo.dto.CiclistaDTO;
import com.example.echo.dto.CiclistaPostDTO;
import com.example.echo.service.CiclistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/ciclista")
public class CiclistaController {

    // Injeta a lógica
    @Autowired
    private CiclistaService service;

    // UC01 A1
    @GetMapping("/existeEmail/{email}")
    public ResponseEntity<Boolean> verificarEmailExistente(
            //pega o email da url
            @PathVariable String email
    ) {
        boolean existe = service.existeEmail(email);

        //200 com verdadeiro ou falso
        return ResponseEntity.ok(existe);
    }

    @PostMapping
    public ResponseEntity<CiclistaDTO> cadastrarCiclista(
            @Valid @RequestBody CiclistaPostDTO ciclistaPostDTO
    ){
        CiclistaDTO ciclistaSalvo = service.cadastrarCiclista(ciclistaPostDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ciclistaSalvo);//201
    }

    //UC02
    @PostMapping("/{idCiclista}/ativar")
    public ResponseEntity<CiclistaDTO> ativarCiclista(
            @PathVariable Long idCiclista
    ){
        CiclistaDTO ciclistaAtivo = service.ativarCiclista(idCiclista);

        return ResponseEntity.ok(ciclistaAtivo);//retorna 200
    }
}