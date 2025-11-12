package com.example.echo.service;

import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.model.Ciclista;
import com.example.echo.model.Nacionalidade;
import com.example.echo.model.StatusCiclista;
import com.example.echo.repository.CiclistaRepository;
import com.example.echo.dto.CiclistaDTO;
import com.example.echo.dto.CiclistaPostDTO;
import com.example.echo.dto.CartaoDeCreditoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CiclistaServiceTest {

    // --- Mocks das Dependências ---
    @Mock
    private CiclistaRepository repository;
    @Mock
    private CiclistaMapper ciclistaMapper;
    @Mock
    private ValidacaoCartaoService validacaoCartaoService;
    @Mock
    private EmailService emailService;

    // --- Serviço sob Teste ---
    @InjectMocks
    private CiclistaService service;

    // --- Dados de Teste (Setup) ---
    private CiclistaPostDTO ciclistaPostDTO;
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

        // A Entidade que o Mapper "criaria"
        ciclistaEntidade = new Ciclista();
        ciclistaEntidade.setId(1L);
        ciclistaEntidade.setEmail("teste@email.com");
        ciclistaEntidade.setStatus(StatusCiclista.AGUARDANDO_CONFIRMACAO);

        // O DTO de resposta que o Mapper "criaria"
        ciclistaDTO = new CiclistaDTO();
        ciclistaDTO.setId(1L);
        ciclistaDTO.setEmail("teste@email.com");
    }

    // ======================================================
    // === Testes do 'cadastrarCiclista' (NOVOS)
    // ======================================================

    @Test
    @DisplayName("Deve cadastrar ciclista com sucesso (Fluxo Principal)")
    void deveCadastrarCiclistaComSucesso() {
        // ARRANGE
        // 1. Validações de duplicidade (passam)
        when(repository.findByEmail(ciclistaPostDTO.getEmail())).thenReturn(Optional.empty());
        when(repository.findByCpf(ciclistaPostDTO.getCpf())).thenReturn(Optional.empty());

        // 2. Validação do Cartão (passa)
        when(validacaoCartaoService.validarCartao(any(CartaoDeCreditoDTO.class))).thenReturn(true);

        // 3. Mapeamento (DTO -> Entidade)
        when(ciclistaMapper.toEntity(ciclistaPostDTO)).thenReturn(ciclistaEntidade);

        // 4. Salvar no Banco (retorna a entidade salva)
        when(repository.save(ciclistaEntidade)).thenReturn(ciclistaEntidade);

        // 5. Mapeamento (Entidade -> DTO Resposta)
        when(ciclistaMapper.toDTO(ciclistaEntidade)).thenReturn(ciclistaDTO);

        // 6. Mock do Email (não faz nada, apenas é chamado)
        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

        // ACT
        CiclistaDTO resultado = service.cadastrarCiclista(ciclistaPostDTO);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(ciclistaDTO.getId(), resultado.getId());
        assertEquals(ciclistaDTO.getEmail(), resultado.getEmail());

        // Verifica se o status foi definido corretamente
        assertEquals(StatusCiclista.AGUARDANDO_CONFIRMACAO, ciclistaEntidade.getStatus());

        // Verifica se todos os mocks foram chamados
        verify(repository, times(1)).findByEmail(ciclistaPostDTO.getEmail());
        verify(repository, times(1)).findByCpf(ciclistaPostDTO.getCpf());
        verify(validacaoCartaoService, times(1)).validarCartao(any(CartaoDeCreditoDTO.class));
        verify(repository, times(1)).save(ciclistaEntidade);
        verify(emailService, times(1)).enviarEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar DadosInvalidosException por Email já cadastrado")
    void deveLancarExcecaoQuandoEmailJaExiste() {
        // ARRANGE
        // Validação de duplicidade (Email falha)
        when(repository.findByEmail(ciclistaPostDTO.getEmail())).thenReturn(Optional.of(new Ciclista()));

        // ACT & ASSERT
        DadosInvalidosException excecao = assertThrows(
                DadosInvalidosException.class,
                () -> service.cadastrarCiclista(ciclistaPostDTO)
        );

        assertEquals("Email já cadastrado.", excecao.getMessage());

        // Garante que o resto do fluxo (CPF, Cartão, Save) NUNCA foi chamado
        verify(repository, never()).findByCpf(any());
        verify(validacaoCartaoService, never()).validarCartao(any());
        verify(repository, never()).save(any());
        verify(emailService, never()).enviarEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar DadosInvalidosException por CPF já cadastrado")
    void deveLancarExcecaoQuandoCpfJaExiste() {
        // ARRANGE
        // Validação de duplicidade (Email passa, CPF falha)
        when(repository.findByEmail(ciclistaPostDTO.getEmail())).thenReturn(Optional.empty());
        when(repository.findByCpf(ciclistaPostDTO.getCpf())).thenReturn(Optional.of(new Ciclista()));

        // ACT & ASSERT
        DadosInvalidosException excecao = assertThrows(
                DadosInvalidosException.class,
                () -> service.cadastrarCiclista(ciclistaPostDTO)
        );

        assertEquals("CPF já cadastrado.", excecao.getMessage());

        // Garante que o resto do fluxo (Cartão, Save) NUNCA foi chamado
        verify(validacaoCartaoService, never()).validarCartao(any());
        verify(repository, never()).save(any());
        verify(emailService, never()).enviarEmail(anyString(), anyString(), anyString());
    }

    // ======================================================
    // === Testes do 'existeEmail' (ANTIGOS)
    // ======================================================

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
        // ARRANGE
        Long idCiclista = 1L;

        // Configura o ciclista de entrada (que o findById vai retornar)
        Ciclista ciclistaPendente = new Ciclista();
        ciclistaPendente.setId(idCiclista);
        ciclistaPendente.setStatus(StatusCiclista.AGUARDANDO_CONFIRMACAO);

        when(repository.findById(idCiclista)).thenReturn(Optional.of(ciclistaPendente));

        // Configura o ciclista de saída (que o save vai retornar)
        Ciclista ciclistaAtivo = new Ciclista();
        ciclistaAtivo.setId(idCiclista);
        ciclistaAtivo.setStatus(StatusCiclista.ATIVO);

        when(repository.save(ciclistaPendente)).thenReturn(ciclistaAtivo);

        // Configura o DTO de resposta
        CiclistaDTO dtoResposta = new CiclistaDTO();
        dtoResposta.setId(idCiclista);
        dtoResposta.setStatus(StatusCiclista.ATIVO);

        when(ciclistaMapper.toDTO(ciclistaAtivo)).thenReturn(dtoResposta);

        // ACT
        CiclistaDTO resultado = service.ativarCiclista(idCiclista);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(StatusCiclista.ATIVO, resultado.getStatus());

        // Verifica se o status e a data foram definidos (Passo 3 do UC02)
        assertNotNull(ciclistaPendente.getDataConfirmacao());
        assertEquals(StatusCiclista.ATIVO, ciclistaPendente.getStatus());

        verify(repository, times(1)).findById(idCiclista);
        verify(repository, times(1)).save(ciclistaPendente);
        verify(ciclistaMapper, times(1)).toDTO(ciclistaAtivo);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar ativar ID inexistente (UC02 - 404)")
    void deveLancar404AoAtivarIdInexistente() {
        // ARRANGE
        Long idInexistente = 99L;
        when(repository.findById(idInexistente)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.ativarCiclista(idInexistente)
        );

        assertEquals("Ciclista não encontrado.", excecao.getMessage());
        verify(repository, never()).save(any()); // Garante que nunca tentou salvar
    }

    @Test
    @DisplayName("Deve lançar DadosInvalidosException ao tentar ativar ciclista que não está pendente (UC02 - E1 - 422)")
    void deveLancar422AoAtivarCiclistaNaoPendente() {
        // ARRANGE
        Long idCiclista = 1L;

        // Ciclista já está ATIVO
        Ciclista ciclistaAtivo = new Ciclista();
        ciclistaAtivo.setId(idCiclista);
        ciclistaAtivo.setStatus(StatusCiclista.ATIVO);

        when(repository.findById(idCiclista)).thenReturn(Optional.of(ciclistaAtivo));

        // ACT & ASSERT
        DadosInvalidosException excecao = assertThrows(
                DadosInvalidosException.class,
                () -> service.ativarCiclista(idCiclista)
        );

        assertEquals("Status de ciclista inválido.", excecao.getMessage());
        verify(repository, never()).save(any()); // Garante que nunca tentou salvar
    }
}