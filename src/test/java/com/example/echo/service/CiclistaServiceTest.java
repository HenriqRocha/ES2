package com.example.echo.service;

import com.example.echo.dto.*;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.model.CartaoDeCredito;
import com.example.echo.model.Ciclista;
import com.example.echo.model.Nacionalidade;
import com.example.echo.model.StatusCiclista;
import com.example.echo.repository.CiclistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CiclistaServiceTest {

    @Mock
    private CiclistaRepository repository;
    @Mock
    private CiclistaMapper ciclistaMapper;
    @Mock
    private ValidacaoCartaoService validacaoCartaoService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private CiclistaService service;

    private CiclistaPostDTO ciclistaPostDTO;
    private CiclistaPutDTO ciclistaPutDTO;
    private Ciclista ciclistaEntidade;
    private CiclistaDTO ciclistaDTO;

    @BeforeEach
    void setUp() {
        // Um DTO de entrada válido (Brasileiro)
        ciclistaPostDTO = new CiclistaPostDTO();
        ciclistaPostDTO.setNome("Ciclista Teste");
        ciclistaPostDTO.setEmail("teste@email.com");
        ciclistaPostDTO.setCpf("12345678901");
        ciclistaPostDTO.setNascimento(LocalDate.of(1990, 1, 1));
        ciclistaPostDTO.setNacionalidade(Nacionalidade.BRASILEIRO);
        ciclistaPostDTO.setSenha("senha123");
        ciclistaPostDTO.setConfirmacaoSenha("senha123");
        ciclistaPostDTO.setMeioDePagamento(new CartaoDeCreditoDTO()); // DTO do cartão

        ciclistaPutDTO = new CiclistaPutDTO();
        ciclistaPutDTO.setNome("Nome Atualizado");

        // A Entidade que o Mapper "criaria"
        ciclistaEntidade = new Ciclista();
        ciclistaEntidade.setId(1L);
        ciclistaEntidade.setEmail("teste@email.com");
        ciclistaEntidade.setStatus(StatusCiclista.AGUARDANDO_CONFIRMACAO);
        ciclistaEntidade.setNacionalidade(Nacionalidade.BRASILEIRO);
        ciclistaEntidade.setCpf("12345678901");
        ciclistaEntidade.setNome("Nome Antigo");

        // O DTO de resposta que o Mapper "criaria"
        ciclistaDTO = new CiclistaDTO();
        ciclistaDTO.setId(1L);
        ciclistaDTO.setEmail("teste@email.com");
    }


    //Cadastrar ciclista
    @Test
    @DisplayName("Deve cadastrar ciclista com sucesso (Fluxo Principal)")
    void deveCadastrarCiclistaComSucesso() {
        //Validações de duplicidade (passam)
        when(repository.findByEmail(ciclistaPostDTO.getEmail())).thenReturn(Optional.empty());
        when(repository.findByCpf(ciclistaPostDTO.getCpf())).thenReturn(Optional.empty());

        //Validação do Cartão (passa)
        when(validacaoCartaoService.validarCartao(any(CartaoDeCreditoDTO.class))).thenReturn(true);

        //Mapeamento (DTO -> Entidade)
        when(ciclistaMapper.toEntity(ciclistaPostDTO)).thenReturn(ciclistaEntidade);

        //Salvar no Banco (retorna a entidade salva)
        when(repository.save(ciclistaEntidade)).thenReturn(ciclistaEntidade);

        //Mapeamento (Entidade -> DTO Resposta)
        when(ciclistaMapper.toDTO(ciclistaEntidade)).thenReturn(ciclistaDTO);

        //Mock do Email
        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

        CiclistaDTO resultado = service.cadastrarCiclista(ciclistaPostDTO);

        assertNotNull(resultado);
        assertEquals(ciclistaDTO.getId(), resultado.getId());
        assertEquals(ciclistaDTO.getEmail(), resultado.getEmail());

        //Verifica se o status foi definido corretamente
        assertEquals(StatusCiclista.AGUARDANDO_CONFIRMACAO, ciclistaEntidade.getStatus());

        //Verifica se todos os mocks foram chamados
        verify(repository, times(1)).findByEmail(ciclistaPostDTO.getEmail());
        verify(repository, times(1)).findByCpf(ciclistaPostDTO.getCpf());
        verify(validacaoCartaoService, times(1)).validarCartao(any(CartaoDeCreditoDTO.class));
        verify(repository, times(1)).save(ciclistaEntidade);
        verify(emailService, times(1)).enviarEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar DadosInvalidosException por Email já cadastrado")
    void deveLancarExcecaoQuandoEmailJaExiste() {
        //validação de email
        when(repository.findByEmail(ciclistaPostDTO.getEmail())).thenReturn(Optional.of(new Ciclista()));

        DadosInvalidosException excecao = assertThrows(
                DadosInvalidosException.class,
                () -> service.cadastrarCiclista(ciclistaPostDTO)
        );

        assertEquals("Email já cadastrado.", excecao.getMessage());

        //o resto não deve ser chamado
        verify(repository, never()).findByCpf(any());
        verify(validacaoCartaoService, never()).validarCartao(any());
        verify(repository, never()).save(any());
        verify(emailService, never()).enviarEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar DadosInvalidosException por CPF já cadastrado")
    void deveLancarExcecaoQuandoCpfJaExiste() {
        //cpf duplicado
        when(repository.findByEmail(ciclistaPostDTO.getEmail())).thenReturn(Optional.empty());
        when(repository.findByCpf(ciclistaPostDTO.getCpf())).thenReturn(Optional.of(new Ciclista()));

        DadosInvalidosException excecao = assertThrows(
                DadosInvalidosException.class,
                () -> service.cadastrarCiclista(ciclistaPostDTO)
        );

        assertEquals("CPF já cadastrado.", excecao.getMessage());

        //O resto não deve ser chamado
        verify(validacaoCartaoService, never()).validarCartao(any());
        verify(repository, never()).save(any());
        verify(emailService, never()).enviarEmail(anyString(), anyString(), anyString());
    }


    //existe email
    @Test
    @DisplayName("Deve lançar DadosInvalidosException para um email nulo")
    void deveLancarExcecaoQuandoEmailForNulo() {
        DadosInvalidosException excecao = assertThrows(
                DadosInvalidosException.class,
                () -> service.existeEmail(null)
        );
        assertEquals("Formato de email inválido.", excecao.getMessage());
        verify(repository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Deve lançar DadosInvalidosException para um email com formato inválido")
    void deveLancarExcecaoQuandoEmailForInvalido() {
        String emailInvalido = "email-sem-arroba.com";
        DadosInvalidosException excecao = assertThrows(
                DadosInvalidosException.class,
                () -> service.existeEmail(emailInvalido)
        );
        assertEquals("Formato de email inválido.", excecao.getMessage());
        verify(repository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Deve retornar false para um email válido que não existe")
    void deveRetornarFalseQuandoEmailValidoNaoExiste() {
        String emailValido = "email@valido.com";
        when(repository.findByEmail(emailValido)).thenReturn(Optional.empty());
        boolean resultado = service.existeEmail(emailValido);
        assertFalse(resultado);
        verify(repository, times(1)).findByEmail(emailValido);
    }

    @Test
    @DisplayName("Deve retornar true para um email válido que já existe")
    void deveRetornarTrueQuandoEmailValidoExiste() {
        String emailExistente = "email@jaexiste.com";
        when(repository.findByEmail(emailExistente)).thenReturn(Optional.of(new Ciclista()));
        boolean resultado = service.existeEmail(emailExistente);
        assertTrue(resultado);
        verify(repository, times(1)).findByEmail(emailExistente);
    }

    //testes ativarCiclista UC02
    @Test
    @DisplayName("Deve ativar ciclista com sucesso (UC02)")
    void deveAtivarCiclistaComSucesso() {
        Long idCiclista = 1L;

        //configura ciclista da entrada
        Ciclista ciclistaPendente = new Ciclista();
        ciclistaPendente.setId(idCiclista);
        ciclistaPendente.setStatus(StatusCiclista.AGUARDANDO_CONFIRMACAO);

        when(repository.findById(idCiclista)).thenReturn(Optional.of(ciclistaPendente));

        //configura o ciclista da saida
        Ciclista ciclistaAtivo = new Ciclista();
        ciclistaAtivo.setId(idCiclista);
        ciclistaAtivo.setStatus(StatusCiclista.ATIVO);

        when(repository.save(ciclistaPendente)).thenReturn(ciclistaAtivo);

        //Configura o DTO de resposta
        CiclistaDTO dtoResposta = new CiclistaDTO();
        dtoResposta.setId(idCiclista);
        dtoResposta.setStatus(StatusCiclista.ATIVO);

        when(ciclistaMapper.toDTO(ciclistaAtivo)).thenReturn(dtoResposta);

        CiclistaDTO resultado = service.ativarCiclista(idCiclista);

        assertNotNull(resultado);
        assertEquals(StatusCiclista.ATIVO, resultado.getStatus());

        //verifica se o status e a data foram definidos
        assertNotNull(ciclistaPendente.getDataConfirmacao());
        assertEquals(StatusCiclista.ATIVO, ciclistaPendente.getStatus());

        verify(repository, times(1)).findById(idCiclista);
        verify(repository, times(1)).save(ciclistaPendente);
        verify(ciclistaMapper, times(1)).toDTO(ciclistaAtivo);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar ativar ID inexistente (UC02 - 404)")
    void deveLancar404AoAtivarIdInexistente() {

        Long idInexistente = 99L;
        when(repository.findById(idInexistente)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.ativarCiclista(idInexistente)
        );

        assertEquals("Ciclista não encontrado.", excecao.getMessage());
        verify(repository, never()).save(any()); //Garante que nunca tentou salvar
    }

    @Test
    @DisplayName("Deve lançar DadosInvalidosException ao tentar ativar ciclista que não está pendente (UC02 - E1 - 422)")
    void deveLancar422AoAtivarCiclistaNaoPendente() {

        Long idCiclista = 1L;

        //Ciclista ativo
        Ciclista ciclistaAtivo = new Ciclista();
        ciclistaAtivo.setId(idCiclista);
        ciclistaAtivo.setStatus(StatusCiclista.ATIVO);

        when(repository.findById(idCiclista)).thenReturn(Optional.of(ciclistaAtivo));

        DadosInvalidosException excecao = assertThrows(
                DadosInvalidosException.class,
                () -> service.ativarCiclista(idCiclista)
        );

        assertEquals("Status de ciclista inválido.", excecao.getMessage());
        verify(repository, never()).save(any()); //Garante que nunca tentou salvar
    }

    //buscar ciclista
    @Test
    @DisplayName("Deve buscar ciclista por ID com sucesso")
    void deveBuscarCiclistaPorId() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.of(ciclistaEntidade));
        when(ciclistaMapper.toDTO(ciclistaEntidade)).thenReturn(ciclistaDTO);

        CiclistaDTO resultado = service.buscarCiclista(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("teste@email.com", resultado.getEmail());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar ciclista inexistente")
    void deveFalharAoBuscarCiclistaInexistente() {
        Long idInexistente = 99L;
        when(repository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () ->
                service.buscarCiclista(idInexistente)
        );
    }

    //atualizar ciclista UC06
    @Test
    @DisplayName("Deve atualizar ciclista com sucesso (alteração simples de nome)")
    void deveAtualizarCiclistaComSucesso() {
        //prepara
        Long id = 1L;
        ciclistaEntidade.setStatus(StatusCiclista.ATIVO);

        // Mocks
        when(repository.findById(id)).thenReturn(Optional.of(ciclistaEntidade));
        when(repository.save(any(Ciclista.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ciclistaMapper.toDTO(any(Ciclista.class))).thenReturn(ciclistaDTO);
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        //Ação
        service.atualizarCiclista(id, ciclistaPutDTO);

        //Verificação
        assertEquals("Nome Atualizado", ciclistaEntidade.getNome()); // Veio do DTO
        assertEquals("12345678901", ciclistaEntidade.getCpf());    // Manteve o original

        verify(repository).save(ciclistaEntidade);
        verify(emailService).enviarEmail(eq(ciclistaEntidade.getEmail()), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve trocar nacionalidade para ESTRANGEIRO e limpar CPF")
    void deveTrocarParaEstrangeiro() {
        Long id = 1L;
        ciclistaEntidade.setStatus(StatusCiclista.ATIVO);

        //DTO pedindo mudança para Estrangeiro
        ciclistaPutDTO.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        PassaporteDTO pass = new PassaporteDTO();
        pass.setNumero("P123");
        pass.setPais("EUA");
        pass.setValidade(LocalDate.now().plusYears(5));
        ciclistaPutDTO.setPassaporte(pass);

        // Mocks
        when(repository.findById(id)).thenReturn(Optional.of(ciclistaEntidade));
        when(repository.save(any(Ciclista.class))).thenAnswer(i -> i.getArgument(0));
        when(ciclistaMapper.toDTO(any())).thenReturn(ciclistaDTO);

        service.atualizarCiclista(id, ciclistaPutDTO);

        // Verificações
        assertEquals(Nacionalidade.ESTRANGEIRO, ciclistaEntidade.getNacionalidade());
        assertNull(ciclistaEntidade.getCpf(), "O CPF deve ser nulo ao virar estrangeiro");
        assertEquals("P123", ciclistaEntidade.getPassaporteNumero());
        assertEquals("EUA", ciclistaEntidade.getPassaportePais());
    }

    @Test
    @DisplayName("Deve trocar nacionalidade para BRASILEIRO e limpar Passaporte")
    void deveTrocarParaBrasileiro() {
        Long id = 1L;
        //Preparar a entidade como se fosse ESTRANGEIRA antes de começar
        ciclistaEntidade.setStatus(StatusCiclista.ATIVO);
        ciclistaEntidade.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        ciclistaEntidade.setPassaporteNumero("OLD999");
        ciclistaEntidade.setCpf(null);

        //DTO pedindo mudança para BRASILEIRO
        ciclistaPutDTO.setNacionalidade(Nacionalidade.BRASILEIRO);
        ciclistaPutDTO.setCpf("98765432100");

        when(repository.findById(id)).thenReturn(Optional.of(ciclistaEntidade));
        when(repository.save(any(Ciclista.class))).thenAnswer(i -> i.getArgument(0));
        when(ciclistaMapper.toDTO(any())).thenReturn(ciclistaDTO);

        service.atualizarCiclista(id, ciclistaPutDTO);

        //Verificações
        assertEquals(Nacionalidade.BRASILEIRO, ciclistaEntidade.getNacionalidade());
        assertEquals("98765432100", ciclistaEntidade.getCpf());
        assertNull(ciclistaEntidade.getPassaporteNumero(), "Passaporte deve ser limpo");
    }

    @Test
    @DisplayName("Deve atualizar apenas campos do Passaporte mantendo nacionalidade ESTRANGEIRO (Fluxo Else)")
    void deveAtualizarCamposPassaporteMantendoEstrangeiro() {
        Long id = 1L;
        //Configura entidade como Estrangeira
        ciclistaEntidade.setStatus(StatusCiclista.ATIVO);
        ciclistaEntidade.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        ciclistaEntidade.setPassaporteNumero("OLD123");
        ciclistaEntidade.setPassaportePais("Argentina");

        //DTO sem mudar nacionalidade (null), mas mandando novo número de passaporte
        ciclistaPutDTO.setNacionalidade(null);
        PassaporteDTO pass = new PassaporteDTO();
        pass.setNumero("NEW999");
        //País null para testar se mantém o antigo
        ciclistaPutDTO.setPassaporte(pass);

        when(repository.findById(id)).thenReturn(Optional.of(ciclistaEntidade));
        when(repository.save(any(Ciclista.class))).thenAnswer(i -> i.getArgument(0));
        when(ciclistaMapper.toDTO(any())).thenReturn(ciclistaDTO);

        service.atualizarCiclista(id, ciclistaPutDTO);

        //Verificações
        assertEquals(Nacionalidade.ESTRANGEIRO, ciclistaEntidade.getNacionalidade());
        assertEquals("NEW999", ciclistaEntidade.getPassaporteNumero()); // Atualizou
        assertEquals("Argentina", ciclistaEntidade.getPassaportePais()); // Manteve antigo
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException se ID não existir")
    void deveLancar404AoAtualizarIdInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.atualizarCiclista(99L, ciclistaPutDTO));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve falhar a atualização se o envio de email falhar (Fluxo E1)")
    void naoDeveFalharSeEmailLancarExcecao() {
        Long id = 1L;
        ciclistaEntidade.setStatus(StatusCiclista.ATIVO);

        when(repository.findById(id)).thenReturn(Optional.of(ciclistaEntidade));
        when(repository.save(any())).thenReturn(ciclistaEntidade);
        when(ciclistaMapper.toDTO(any())).thenReturn(ciclistaDTO);

        //Simula erro no serviço de email
        doThrow(new RuntimeException("Erro SMTP")).when(emailService).enviarEmail(any(), any(), any());

        //O método deve terminar sem lançar exceção
        assertDoesNotThrow(() -> service.atualizarCiclista(id, ciclistaPutDTO));

        //E os dados devem ter sido salvos
        verify(repository, times(1)).save(ciclistaEntidade);
    }

    //testes de cartão
    @Test
    @DisplayName("Deve buscar cartão com sucesso")
    void deveBuscarCartaoComSucesso() {
        Long id = 1L;

        CartaoDeCredito cartao = new CartaoDeCredito();
        cartao.setNomeTitular("Titular Teste");
        cartao.setNumero("1234");
        ciclistaEntidade.setCartaoDeCredito(cartao);

        when(repository.findById(id)).thenReturn(Optional.of(ciclistaEntidade));

        CartaoDeCreditoDTO resultado = service.buscarCartao(id);

        assertNotNull(resultado);
        assertEquals("Titular Teste", resultado.getNomeTitular());
        assertEquals("1234", resultado.getNumero());
    }

    @Test
    @DisplayName("Deve alterar cartão com sucesso (Validação Externa OK)")
    void deveAlterarCartaoComSucesso() {
        Long id = 1L;
        CartaoDeCreditoDTO novoCartao = new CartaoDeCreditoDTO();
        novoCartao.setNumero("4444");
        novoCartao.setNomeTitular("Novo Titular");

        when(repository.findById(id)).thenReturn(Optional.of(ciclistaEntidade));

        when(validacaoCartaoService.validarCartao(any(CartaoDeCreditoDTO.class))).thenReturn(true);

        doNothing().when(emailService).enviarEmail(any(), any(), any());

        service.alterarCartao(id, novoCartao);

        //vendo se o mapper é acionado corretamente
        verify(ciclistaMapper).updateCartaoFromDTO(eq(novoCartao), eq(ciclistaEntidade));

        //salvou
        verify(repository).save(ciclistaEntidade);
    }

    @Test
    @DisplayName("Deve lançar exceção se o cartão for reprovado pela validação externa")
    void deveFalharSeCartaoInvalido() {
        Long id = 1L;
        CartaoDeCreditoDTO novoCartao = new CartaoDeCreditoDTO();

        when(repository.findById(id)).thenReturn(Optional.of(ciclistaEntidade));

        //validação falsa
        when(validacaoCartaoService.validarCartao(any())).thenReturn(false);

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.alterarCartao(id, novoCartao)
        );

        assertEquals("O cartão de crédito foi reprovado pela operadora.", ex.getMessage());

        // Garante que nao salvou
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar 404 ao tentar alterar cartão de ciclista inexistente")
    void deveFalharAlterarCartaoIdInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () ->
                service.alterarCartao(99L, new CartaoDeCreditoDTO())
        );
    }
}