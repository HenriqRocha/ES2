package com.example.echo.service;

import com.example.echo.dto.CartaoDeCreditoDTO;
import org.springframework.stereotype.Service;

//API falsa
@Service
public class ValidacaoCartaoService {

    public boolean validarCartao(CartaoDeCreditoDTO cartaoDTO){
        cartaoDTO.getCvv();
        return true;
    }
}
