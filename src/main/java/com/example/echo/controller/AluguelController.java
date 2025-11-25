package com.example.echo.controller;

import com.example.echo.dto.AluguelDTO;
import com.example.echo.dto.BicicletaDTO;
import com.example.echo.dto.DevolucaoDTO;
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

    // GET /ciclista/{idCiclista}/permiteAluguel
    @GetMapping("/ciclista/{idCiclista}/permiteAluguel")
    public ResponseEntity<Boolean> permiteAluguel(@PathVariable Long idCiclista) {
        boolean podeAlugar = aluguelService.permiteAluguel(idCiclista);
        return ResponseEntity.ok(podeAlugar);
    }

    // GET /ciclista/{idCiclista}/bicicletaAlugada
    @GetMapping("/ciclista/{idCiclista}/bicicletaAlugada")
    public ResponseEntity<BicicletaDTO> buscarBicicletaAlugada(@PathVariable Long idCiclista) {
        BicicletaDTO bicicleta = aluguelService.buscarBicicletaAlugada(idCiclista);

        // Retorna 200 OK mesmo que seja null (body vazio), conforme swagger
        return ResponseEntity.ok(bicicleta);
    }

    //POST aluguel/devolucao UC04
    @PostMapping("/devolucao")
    public ResponseEntity<AluguelDTO> realizarDevolucao(
            @Valid @RequestBody DevolucaoDTO devolucaoDTO
    ) {
        AluguelDTO aluguelFinalizado = aluguelService.realizarDevolucao(devolucaoDTO);
        return ResponseEntity.ok(aluguelFinalizado);
    }
}