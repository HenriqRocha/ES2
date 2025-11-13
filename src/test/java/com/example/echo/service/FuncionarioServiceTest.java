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

    private Funcionario funcionario;
    private NovoFuncionarioDTO novoFuncionarioDTO;
    private Long idExistente = 1L;
    private Long idInexistente = 99L;

    @BeforeEach
    void setUp() {
        // DTO
        novoFuncionarioDTO = new NovoFuncionarioDTO();
        novoFuncionarioDTO.setNome("João Teste");
        novoFuncionarioDTO.setEmail("joao@teste.com");
        novoFuncionarioDTO.setCpf("12345678901");
        novoFuncionarioDTO.setFuncao(FuncaoFuncionario.ADMINISTRATIVO);
        novoFuncionarioDTO.setIdade(30);
        novoFuncionarioDTO.setSenha("123");
        novoFuncionarioDTO.setConfirmacaoSenha("123");

        // Entidade resposta do repositório falso
        funcionario = new Funcionario();
        funcionario.setId(idExistente);
        funcionario.setNome("João Teste");
        funcionario.setEmail("joao@teste.com");
        funcionario.setCpf("12345678901");
        funcionario.setFuncao(FuncaoFuncionario.ADMINISTRATIVO);
        funcionario.setIdade(30);
        funcionario.setSenha("123");
    }

    //Cadastrar
    @Test
    @DisplayName("Deve cadastrar um novo funcionário com sucesso")
    void deveCadastrarFuncionarioComSucesso() {

        when(repository.save(any(Funcionario.class))).thenReturn(funcionario);

        FuncionarioDTO resultado = service.cadastrarFuncionario(novoFuncionarioDTO);

        assertNotNull(resultado);
        assertEquals(idExistente, resultado.getMatricula());
        assertEquals("João Teste", resultado.getNome());
        verify(repository, times(1)).save(any(Funcionario.class));
    }

    //buscar funcionario
    @Test
    @DisplayName("Deve buscar um funcionário por ID com sucesso")
    void deveBuscarFuncionarioPorIdComSucesso() {

        when(repository.findById(idExistente)).thenReturn(Optional.of(funcionario));

        FuncionarioDTO resultado = service.buscarFuncionarioPorId(idExistente);

        assertNotNull(resultado);
        assertEquals(idExistente, resultado.getMatricula());
        verify(repository, times(1)).findById(idExistente);
    }

    //Busca funcionario falha
    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao buscar ID que não existe")
    void deveLancarExcecaoAoBuscarIdInexistente() {

        when(repository.findById(idInexistente)).thenReturn(Optional.empty());


        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.buscarFuncionarioPorId(idInexistente)
        );

        assertEquals("Funcionário não encontrado com ID: " + idInexistente, excecao.getMessage());
        verify(repository, times(1)).findById(idInexistente);
    }

    //listar todos os funcionários
    @Test
    @DisplayName("Deve listar todos os funcionários")
    void deveListarTodosFuncionarios() {

        when(repository.findAll()).thenReturn(Collections.singletonList(funcionario));

        List<FuncionarioDTO> resultado = service.listarFuncionarios();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("João Teste", resultado.get(0).getNome());
        verify(repository, times(1)).findAll();
    }

    //atualizar
    @Test
    @DisplayName("Deve atualizar um funcionário com sucesso")
    void deveAtualizarFuncionarioComSucesso() {

        when(repository.findById(idExistente)).thenReturn(Optional.of(funcionario));
        when(repository.save(any(Funcionario.class))).thenReturn(funcionario);

        // DTO com dados atualizados
        novoFuncionarioDTO.setNome("João Atualizado");

        FuncionarioDTO resultado = service.atualizarFuncionario(idExistente, novoFuncionarioDTO);

        assertNotNull(resultado);
        assertEquals("João Atualizado", resultado.getNome()); //Verifica se o nome mudou
        verify(repository, times(1)).findById(idExistente);
        verify(repository, times(1)).save(any(Funcionario.class));
    }

    //Atualiza falha
    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar atualizar ID que não existe")
    void deveLancarExcecaoAoAtualizarIdInexistente() {

        when(repository.findById(idInexistente)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.atualizarFuncionario(idInexistente, novoFuncionarioDTO)
        );

        assertEquals("Funcionário não encontrado com ID: " + idInexistente, excecao.getMessage());
        verify(repository, times(1)).findById(idInexistente);
        verify(repository, never()).save(any(Funcionario.class)); // Garante que o 'save' NUNCA foi chamado
    }

    //deletar
    @Test
    @DisplayName("Deve deletar um funcionário com sucesso")
    void deveDeletarFuncionarioComSucesso() {

        when(repository.existsById(idExistente)).thenReturn(true);

        doNothing().when(repository).deleteById(idExistente);

        //verifica se nenhuma exceção foi lançada
        assertDoesNotThrow(() -> service.deletarFuncionario(idExistente));

        //Verifica se os métodos corretos foram chamados
        verify(repository, times(1)).existsById(idExistente);
        verify(repository, times(1)).deleteById(idExistente);
    }

    //deletar falha
    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar deletar ID que não existe")
    void deveLancarExcecaoAoDeletarIdInexistente() {

        when(repository.existsById(idInexistente)).thenReturn(false);

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.deletarFuncionario(idInexistente)
        );

        assertEquals("Funcionário não encontrado com ID: " + idInexistente, excecao.getMessage());
        verify(repository, times(1)).existsById(idInexistente);
        verify(repository, never()).deleteById(anyLong()); //Garante que o 'delete' nunca foi chamado
    }
}