package com.example.echo.service;

import org.springframework.stereotype.Service;
@Service
public class EmailService {

    public void enviarEmail(String email, String assunto, String corpo){
        System.out.println("EMAIL");
        System.out.println("Para: "+ email);
        System.out.println("Assunto: "+ assunto);
        System.out.println("Corpo: "+corpo);
        System.out.println("-----------------------------");
    }
}
