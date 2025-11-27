package com.example.echo.controller;

import com.example.echo.dto.CartaoDeCreditoDTO;
import com.example.echo.service.CiclistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/cartaoDeCredito")
public class CartaoDeCreditoController {

    @Autowired
    private CiclistaService ciclistaService;

    //get
    @GetMapping("/{idCiclista}")
    public ResponseEntity<CartaoDeCreditoDTO> buscarCartao(@PathVariable Long idCiclista) {
        CartaoDeCreditoDTO cartao = ciclistaService.buscarCartao(idCiclista);

        //200 com body vazio
        return ResponseEntity.ok(cartao);
    }

    //put
    @PutMapping("/{idCiclista}")
    public ResponseEntity<Void> alterarCartao(
            @PathVariable Long idCiclista,
            @Valid @RequestBody CartaoDeCreditoDTO novoCartao
    ) {
        //chama serviço do cartão
        ciclistaService.alterarCartao(idCiclista, novoCartao);

        //200
        return ResponseEntity.ok().build();
    }
}