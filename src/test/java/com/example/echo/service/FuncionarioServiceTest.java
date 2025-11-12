package com.example.echo.service;

import com.example.echo.dto.FuncionarioDTO;
import com.example.echo.dto.NovoFuncionarioDTO;
import com.example.echo.exception.RecursoNaoEncontradoException;
import com.example.echo.model.FuncaoFuncionario;
import com.example.echo.model.Funcionario;
import com.example.echo.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections; // Importar!
import java.util.List; // Importar!
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository repository;

    @InjectMocks
    private FuncionarioService service;

    // Variáveis de teste que vamos usar em vários testes
    private Funcionario funcionario;
    private NovoFuncionarioDTO novoFuncionarioDTO;
    private Long idExistente = 1L;
    private Long idInexistente = 99L;

    @BeforeEach
    void setUp() {
        // DTO (usado para criar e atualizar)
        novoFuncionarioDTO = new NovoFuncionarioDTO();
        novoFuncionarioDTO.setNome("João Teste");
        novoFuncionarioDTO.setEmail("joao@teste.com");
        novoFuncionarioDTO.setCpf("12345678901");
        novoFuncionarioDTO.setFuncao(FuncaoFuncionario.ADMINISTRATIVO);
        novoFuncionarioDTO.setIdade(30);
        novoFuncionarioDTO.setSenha("123");
        novoFuncionarioDTO.setConfirmacaoSenha("123");

        // Entidade (o que o repositório "falso" vai retornar)
        funcionario = new Funcionario();
        funcionario.setId(idExistente);
        funcionario.setNome("João Teste");
        funcionario.setEmail("joao@teste.com");
        funcionario.setCpf("12345678901");
        funcionario.setFuncao(FuncaoFuncionario.ADMINISTRATIVO);
        funcionario.setIdade(30);
        funcionario.setSenha("123");
    }

    // --- TESTE 1: CADASTRAR (Caminho Feliz) ---
    @Test
    @DisplayName("Deve cadastrar um novo funcionário com sucesso")
    void deveCadastrarFuncionarioComSucesso() {
        // ARRANGE (Já feito no setUp)
        // Diga ao mock para retornar o funcionário com ID quando 'save' for chamado
        when(repository.save(any(Funcionario.class))).thenReturn(funcionario);

        // ACT
        FuncionarioDTO resultado = service.cadastrarFuncionario(novoFuncionarioDTO);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(idExistente, resultado.getMatricula());
        assertEquals("João Teste", resultado.getNome());
        verify(repository, times(1)).save(any(Funcionario.class));
    }

    // --- TESTE 2: BUSCAR POR ID (Caminho Feliz) ---
    @Test
    @DisplayName("Deve buscar um funcionário por ID com sucesso")
    void deveBuscarFuncionarioPorIdComSucesso() {
        // ARRANGE
        // Diga ao mock para retornar o funcionário quando 'findById' for chamado
        when(repository.findById(idExistente)).thenReturn(Optional.of(funcionario));

        // ACT
        FuncionarioDTO resultado = service.buscarFuncionarioPorId(idExistente);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(idExistente, resultado.getMatricula());
        verify(repository, times(1)).findById(idExistente);
    }

    // --- TESTE 3: BUSCAR POR ID (Caminho Triste / 404) ---
    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao buscar ID que não existe")
    void deveLancarExcecaoAoBuscarIdInexistente() {
        // ARRANGE
        // Diga ao mock para retornar vazio quando 'findById' for chamado
        when(repository.findById(idInexistente)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.buscarFuncionarioPorId(idInexistente)
        );

        assertEquals("Funcionário não encontrado com ID: " + idInexistente, excecao.getMessage());
        verify(repository, times(1)).findById(idInexistente);
    }

    // --- TESTE 4: LISTAR TODOS (Caminho Feliz) ---
    @Test
    @DisplayName("Deve listar todos os funcionários")
    void deveListarTodosFuncionarios() {
        // ARRANGE
        // Diga ao mock para retornar uma lista com o nosso funcionário
        when(repository.findAll()).thenReturn(Collections.singletonList(funcionario));

        // ACT
        List<FuncionarioDTO> resultado = service.listarFuncionarios();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size()); // Esperamos 1 item na lista
        assertEquals("João Teste", resultado.get(0).getNome());
        verify(repository, times(1)).findAll();
    }

    // --- TESTE 5: ATUALIZAR (Caminho Feliz) ---
    @Test
    @DisplayName("Deve atualizar um funcionário com sucesso")
    void deveAtualizarFuncionarioComSucesso() {
        // ARRANGE
        // Diga ao mock para (1) encontrar o funcionário e (2) salvá-lo
        when(repository.findById(idExistente)).thenReturn(Optional.of(funcionario));
        when(repository.save(any(Funcionario.class))).thenReturn(funcionario);

        // DTO com dados atualizados (embora sejam os mesmos do setup, a lógica é testada)
        novoFuncionarioDTO.setNome("João Atualizado");

        // ACT
        FuncionarioDTO resultado = service.atualizarFuncionario(idExistente, novoFuncionarioDTO);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("João Atualizado", resultado.getNome()); // Verifica se o nome mudou
        verify(repository, times(1)).findById(idExistente);
        verify(repository, times(1)).save(any(Funcionario.class));
    }

    // --- TESTE 6: ATUALIZAR (Caminho Triste / 404) ---
    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar atualizar ID que não existe")
    void deveLancarExcecaoAoAtualizarIdInexistente() {
        // ARRANGE
        // Diga ao mock para NÃO encontrar o funcionário
        when(repository.findById(idInexistente)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.atualizarFuncionario(idInexistente, novoFuncionarioDTO)
        );

        assertEquals("Funcionário não encontrado com ID: " + idInexistente, excecao.getMessage());
        verify(repository, times(1)).findById(idInexistente);
        verify(repository, never()).save(any(Funcionario.class)); // Garante que o 'save' NUNCA foi chamado
    }

    // --- TESTE 7: DELETAR (Caminho Feliz) ---
    @Test
    @DisplayName("Deve deletar um funcionário com sucesso")
    void deveDeletarFuncionarioComSucesso() {
        // ARRANGE
        // Diga ao mock que o ID existe
        when(repository.existsById(idExistente)).thenReturn(true);
        // Diga ao mock para não fazer nada quando 'deleteById' for chamado
        doNothing().when(repository).deleteById(idExistente);

        // ACT & ASSERT (Verifica se NENHUMA exceção foi lançada)
        assertDoesNotThrow(() -> service.deletarFuncionario(idExistente));

        // Verifica se os métodos corretos foram chamados
        verify(repository, times(1)).existsById(idExistente);
        verify(repository, times(1)).deleteById(idExistente);
    }

    // --- TESTE 8: DELETAR (Caminho Triste / 404) ---
    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar deletar ID que não existe")
    void deveLancarExcecaoAoDeletarIdInexistente() {
        // ARRANGE
        // Diga ao mock que o ID não existe
        when(repository.existsById(idInexistente)).thenReturn(false);

        // ACT & ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.deletarFuncionario(idInexistente)
        );

        assertEquals("Funcionário não encontrado com ID: " + idInexistente, excecao.getMessage());
        verify(repository, times(1)).existsById(idInexistente);
        verify(repository, never()).deleteById(anyLong()); // Garante que o 'delete' NUNCA foi chamado
    }
}