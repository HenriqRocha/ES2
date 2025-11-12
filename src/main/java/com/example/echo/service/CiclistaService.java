package com.example.echo.service;

import com.example.echo.model.Ciclista;
import com.example.echo.model.StatusCiclista;

import java.time.LocalDateTime;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.repository.CiclistaRepository;
import com.example.echo.dto.CiclistaDTO;
import com.example.echo.dto.CiclistaPostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service // 1. Diz ao Spring que esta é uma classe de Serviço (um "Bean")
public class CiclistaService {

    // 2. Injeta o Repositório para podermos falar com o banco
    @Autowired
    private CiclistaRepository repository;

    @Autowired
    private CiclistaMapper ciclistaMapper;//tradutor

    @Autowired
    private ValidacaoCartaoService validacaoCartaoService;//api falsa

    @Autowired
    private EmailService emailService;//api falsa

    //regex para validar formato de email R3 e poder retornar 422
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

    /**
     * Lógica para o UC01 - Fluxo Alternativo [A1]
     * Verifica se um email já está cadastrado no banco de dados.
     *
     * @param email O email a ser verificado.
     * @return true se o email já existe, false caso contrário.
     * @throws DadosInvalidosException se o formato for inválido
     */
    public boolean existeEmail(String email) {
        //R3
        if (!isEmaivalido(email)){
            throw new DadosInvalidosException("Formato de email inválido.");
        }

        return repository.findByEmail(email).isPresent();
    }

    //cadastra novo ciclista
    public CiclistaDTO cadastrarCiclista(CiclistaPostDTO dto){
        //email existente
        if (repository.findByEmail(dto.getEmail()).isPresent()){
            throw new DadosInvalidosException("Email já cadastrado.");
        }
        if (dto.getCpf() != null && repository.findByCpf(dto.getCpf()).isPresent()){
            throw new DadosInvalidosException("CPF já cadastrado.");
        }

        //validação cartão
        boolean cartaoValido = validacaoCartaoService.validarCartao(dto.getMeioDePagamento());
        if (!cartaoValido){
            throw new DadosInvalidosException("Cartão de crédito reprovado.");
        }

        //tradução dto para entidade
        Ciclista ciclista = ciclistaMapper.toEntity(dto);

        //definindo status
        ciclista.setStatus(StatusCiclista.AGUARDANDO_CONFIRMACAO);

        //salvando novo ciclista
        Ciclista ciclistaSalvo = repository.save(ciclista);

        //envia email
        emailService.enviarEmail(
                ciclistaSalvo.getEmail(),
                "Confimação de cadastro",
                "Bem-vindo, confirme email por favor");

        //tradução entidade para dto de resposta
        return ciclistaMapper.toDTO(ciclistaSalvo);
    }

    //UC02
    public CiclistaDTO ativarCiclista(Long idciclista){
        //cicilsta existe ou 404
        Ciclista ciclista = repository.findById(idciclista)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclista não encontrado."));

        //verificar status E1 ou 422
        if (ciclista.getStatus() != StatusCiclista.AGUARDANDO_CONFIRMACAO){
            throw new DadosInvalidosException("Status de ciclista inválido.");
        }

        //principal
        ciclista.setStatus(StatusCiclista.ATIVO);
        ciclista.setDataConfirmacao(LocalDateTime.now());

        Ciclista ciclistaAtivo = repository.save(ciclista);

        return ciclistaMapper.toDTO(ciclistaAtivo);
    }

    //métodos auxiliares

    private boolean isEmaivalido(String email){
        if (email == null || email.isEmpty()){return false;}

        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
}