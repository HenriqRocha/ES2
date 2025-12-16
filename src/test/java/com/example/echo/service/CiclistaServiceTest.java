package com.example.echo.service;

import com.example.echo.dto.*;
import com.example.echo.dto.externo.CartaoExternoDTO;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.model.CartaoDeCredito;
import com.example.echo.model.Ciclista;
import com.example.echo.model.Nacionalidade;
import com.example.echo.model.StatusCiclista;
import com.example.echo.repository.AluguelRepository;
import com.example.echo.repository.CiclistaRepository;
import com.example.echo.service.externo.ExternoClient;
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
    private AluguelRepository aluguelRepository; // Adicionado pois existe no Service

    @Mock
    private CiclistaMapper ciclistaMapper;

    @Mock
    private ExternoClient externoClient; // Substituiu o ValidacaoCartaoService

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

        CartaoDeCreditoDTO cartaoDTO = new CartaoDeCreditoDTO();
        cartaoDTO.setNomeTitular("Teste");
        cartaoDTO.setNumero("1234");
        cartaoDTO.setValidade(LocalDate.now().plusYears(2));
        cartaoDTO.setCvv("123");
        ciclistaPostDTO.setMeioDePagamento(cartaoDTO);

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

        //Validação do Cartão (USANDO O NOVO CLIENT)
        when(externoClient.validarCartao(any(CartaoExternoDTO.class))).thenReturn(true);

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

        //Verifica se o status foi definido corretamente
        assertEquals(StatusCiclista.AGUARDANDO_CONFIRMACAO, ciclistaEntidade.getStatus());

        //Verifica se todos os mocks foram chamados
        verify(repository, times(1)).findByEmail(ciclistaPostDTO.getEmail());
        verify(repository, times(1)).findByCpf(ciclistaPostDTO.getCpf());
        // Alterado para ExternoClient
        verify(externoClient, times(1)).validarCartao(any(CartaoExternoDTO.class));
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
        verify(externoClient, never()).validarCartao(any());
        verify(repository, never()).save(any());
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

        verify(externoClient, never()).validarCartao(any());
        verify(repository, never()).save(any());
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
    @DisplayName("Deve retornar false para um email válido que não existe")
    void deveRetornarFalseQuandoEmailValidoNaoExiste() {
        String emailValido = "email@valido.com";
        when(repository.findByEmail(emailValido)).thenReturn(Optional.empty());
        boolean resultado = service.existeEmail(emailValido);
        assertFalse(resultado);
        verify(repository, times(1)).findByEmail(emailValido);
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

        verify(repository, times(1)).save(ciclistaPendente);
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
    }

    //atualizar ciclista UC06
    @Test
    @DisplayName("Deve atualizar ciclista com sucesso (alteração simples de nome)")
    void deveAtualizarCiclistaComSucesso() {
        Long id = 1L;
        ciclistaEntidade.setStatus(StatusCiclista.ATIVO);

        when(repository.findById(id)).thenReturn(Optional.of(ciclistaEntidade));
        when(repository.save(any(Ciclista.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ciclistaMapper.toDTO(any(Ciclista.class))).thenReturn(ciclistaDTO);
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        service.atualizarCiclista(id, ciclistaPutDTO);

        assertEquals("Nome Atualizado", ciclistaEntidade.getNome());
        verify(repository).save(ciclistaEntidade);
    }

    //testes de cartão
    @Test
    @DisplayName("Deve alterar cartão com sucesso (Validação Externa OK)")
    void deveAlterarCartaoComSucesso() {
        Long id = 1L;
        CartaoDeCreditoDTO novoCartao = new CartaoDeCreditoDTO();
        novoCartao.setNumero("4444");
        novoCartao.setNomeTitular("Novo Titular");
        novoCartao.setValidade(LocalDate.now().plusYears(1)); // Importante ter data para converter
        novoCartao.setCvv("123");

        when(repository.findById(id)).thenReturn(Optional.of(ciclistaEntidade));

        // MOCK CORRIGIDO: Usa ExternoClient e CartaoExternoDTO
        when(externoClient.validarCartao(any(CartaoExternoDTO.class))).thenReturn(true);

        doNothing().when(emailService).enviarEmail(any(), any(), any());

        service.alterarCartao(id, novoCartao);

        verify(ciclistaMapper).updateCartaoFromDTO(eq(novoCartao), eq(ciclistaEntidade));
        verify(repository).save(ciclistaEntidade);
    }

    @Test
    @DisplayName("Deve lançar exceção se o cartão for reprovado pela validação externa")
    void deveFalharSeCartaoInvalido() {
        Long id = 1L;
        CartaoDeCreditoDTO novoCartao = new CartaoDeCreditoDTO();
        novoCartao.setNomeTitular("Teste");
        novoCartao.setNumero("123");
        novoCartao.setValidade(LocalDate.now());

        when(repository.findById(id)).thenReturn(Optional.of(ciclistaEntidade));

        // MOCK CORRIGIDO: Usa ExternoClient e CartaoExternoDTO
        when(externoClient.validarCartao(any(CartaoExternoDTO.class))).thenReturn(false);

        DadosInvalidosException ex = assertThrows(DadosInvalidosException.class, () ->
                service.alterarCartao(id, novoCartao)
        );

        assertEquals("O cartão de crédito foi reprovado pela operadora.", ex.getMessage());
        verify(repository, never()).save(any());
    }
}