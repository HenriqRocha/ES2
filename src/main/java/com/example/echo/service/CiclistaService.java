package com.example.echo.service;

import com.example.echo.dto.*;
import com.example.echo.dto.externo.CartaoExternoDTO;
import com.example.echo.model.CartaoDeCredito;
import com.example.echo.model.Ciclista;
import com.example.echo.model.Nacionalidade;
import com.example.echo.model.StatusCiclista;
import com.example.echo.model.Aluguel;

import java.time.LocalDateTime;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.repository.AluguelRepository;
import com.example.echo.repository.CiclistaRepository;
import com.example.echo.service.externo.ExternoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CiclistaService {

    @Autowired
    private CiclistaRepository repository;

    @Autowired
    private AluguelRepository aluguelRepository;

    @Autowired
    private CiclistaMapper ciclistaMapper;//tradutor

    @Autowired
    private EmailService emailService;

    @Autowired
    private ExternoClient externoClient;

    //regex para validar formato de email R3 e poder retornar 422
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

    //UC01 - A1
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

        CartaoDeCreditoDTO cartaoInterno = dto.getMeioDePagamento();
        CartaoExternoDTO cartaoParaValidar = new CartaoExternoDTO(
                cartaoInterno.getNomeTitular(),
                cartaoInterno.getNumero(),
                cartaoInterno.getValidade().toString(),
                cartaoInterno.getCvv()
        );

        boolean cartaoValido = externoClient.validarCartao(cartaoParaValidar);

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

    //recupera dados do ciclista
    public CiclistaDTO buscarCiclista(Long id) {
        Ciclista ciclista = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclista não encontrado"));
        return ciclistaMapper.toDTO(ciclista);
    }

    //UC06 PERGUNTAR SOBRE DADOS INVÁLIDOS
    public CiclistaDTO atualizarCiclista(Long id, CiclistaPutDTO dados){
        Ciclista ciclista = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclista não encontrado"));

        if (dados.getNome() != null){
            ciclista.setNome(dados.getNome());
        }

        if (dados.getUrlFotoDocumento() != null){
            ciclista.setUrlFotoDocumento(dados.getUrlFotoDocumento());
        }

        //valida a senha pelo @senhasiguais
        if (dados.getSenha() != null && !dados.getSenha().isBlank()){
            ciclista.setSenha(dados.getSenha());
        }

        if (dados.getNacionalidade() != null){//mudou nacionalidade
            ciclista.setNacionalidade(dados.getNacionalidade());

            if (dados.getNacionalidade() == Nacionalidade.BRASILEIRO) {
                if (dados.getCpf() != null) {
                    ciclista.setCpf(dados.getCpf());
                }
                ciclista.setPassaporteValidade(null);
                ciclista.setPassaporteNumero(null);
                ciclista.setPassaportePais(null);
            }
            else {
                if (dados.getPassaporte() != null){
                    if (dados.getPassaporte().getNumero() != null){
                        ciclista.setPassaporteNumero(dados.getPassaporte().getNumero());
                    }

                    if (dados.getPassaporte().getPais() != null){
                        ciclista.setPassaportePais(dados.getPassaporte().getPais());
                    }
                    if (dados.getPassaporte().getValidade() != null){
                        ciclista.setPassaporteValidade(dados.getPassaporte().getValidade());
                    }
                }
                ciclista.setCpf(null);
            }
        } else {//mudou cpf ou passaporte apenas
            if (ciclista.getNacionalidade() == Nacionalidade.BRASILEIRO && dados.getCpf() != null){
                ciclista.setCpf(dados.getCpf());
            } else if (ciclista.getNacionalidade() == Nacionalidade.ESTRANGEIRO && dados.getPassaporte() != null) {
                if (dados.getPassaporte().getNumero() != null){
                    ciclista.setPassaporteNumero(dados.getPassaporte().getNumero());
                }

                if (dados.getPassaporte().getPais() != null){
                    ciclista.setPassaportePais(dados.getPassaporte().getPais());
                }
                if (dados.getPassaporte().getValidade() != null){
                    ciclista.setPassaporteValidade(dados.getPassaporte().getValidade());
                }
            }
        }
        Ciclista salvo = repository.save(ciclista);
        enviarEmailAtualizacao(salvo);
        return ciclistaMapper.toDTO(salvo);
    }

    // GETRecupera os dados do cartão
    public CartaoDeCreditoDTO buscarCartao(Long idCiclista) {
        Ciclista ciclista = repository.findById(idCiclista)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclista não encontrado"));

        CartaoDeCreditoDTO dto = new CartaoDeCreditoDTO();
        dto.setNomeTitular(ciclista.getCartaoDeCredito().getNomeTitular());
        dto.setNumero(ciclista.getCartaoDeCredito().getNumero());
        dto.setValidade(ciclista.getCartaoDeCredito().getValidade());
        dto.setCvv(ciclista.getCartaoDeCredito().getCvv());

        return dto;
    }

    // PUT: altera cartão
    public void alterarCartao(Long idCiclista, CartaoDeCreditoDTO novoCartao) {
        Ciclista ciclista = repository.findById(idCiclista)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclista não encontrado"));

        CartaoExternoDTO cartaoParaValidar = new CartaoExternoDTO(
                novoCartao.getNomeTitular(),
                novoCartao.getNumero(),
                novoCartao.getValidade().toString(), // Converte Data para String
                novoCartao.getCvv()
        );

        boolean cartaoValido = externoClient.validarCartao(cartaoParaValidar);

        if (!cartaoValido) {
            throw new DadosInvalidosException("O cartão de crédito foi reprovado pela operadora.");
        }

        //atualiza usando o mapper
        ciclistaMapper.updateCartaoFromDTO(novoCartao, ciclista);

        repository.save(ciclista);

        try {
            emailService.enviarEmail(ciclista.getEmail(), "Cartão Atualizado", "Dados de pagamento alterados.");
        } catch (Exception e) {
            System.err.println("Erro email: " + e.getMessage());
        }
    }

    public boolean permiteAluguel(Long ciclistaId) {
        Ciclista ciclista = repository.findById(ciclistaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ciclista não encontrado"));

        //só pode alugar ativo
        if (ciclista.getStatus() != StatusCiclista.ATIVO) {
            return false;
        }

        //não pode alugar ao mesmo tempo
        boolean temAluguelAtivo = aluguelRepository.existsByCiclistaIdAndHoraFimIsNull(ciclistaId);

        return !temAluguelAtivo; //TRUE se não tiver aluguel (pode alugar)
    }

    public BicicletaDTO buscarBicicletaAlugada(Long ciclistaId) {
        if (!repository.existsById(ciclistaId)) {
            throw new RecursoNaoEncontradoException("Ciclista não encontrado");
        }

        //busca aluguel
        Optional<Aluguel> aluguelOpt = aluguelRepository.findByCiclistaIdAndHoraFimIsNull(ciclistaId);

        if (aluguelOpt.isPresent()) {
            //retorna a bike do aluguel
            Long idBike = aluguelOpt.get().getBicicleta();
            return new BicicletaDTO(idBike, "EM_USO");
        }

        //se não tiver
        return null;
    }

    //métodos auxiliares

    private boolean isEmaivalido(String email){
        if (email == null || email.isEmpty()){return false;}

        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    private void enviarEmailAtualizacao(Ciclista ciclista) {
        String mensagem = "Olá " + ciclista.getNome() + ", seus dados cadastrais foram alterados com sucesso.";

        try {
            emailService.enviarEmail(ciclista.getEmail(),"Atualização de dados", mensagem);
        } catch (Exception e) {
            //E1 avisa a falha mas não cancela a atualização
            System.err.println("Falha ao enviar e-mail para " + ciclista.getEmail() + ": " + e.getMessage());
        }
    }


}