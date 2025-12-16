package com.example.echo.service;

import com.example.echo.service.externo.ExternoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class EmailService {

    @Autowired
    private ExternoClient externoClient;

    public void enviarEmail(String email, String assunto, String mensagem) {
        externoClient.enviarEmail(email, assunto, mensagem);
    }
}
