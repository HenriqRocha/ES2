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

    @BeforeEach
    void setUp() {
        novoFuncionarioDTO = new NovoFuncionarioDTO();
        novoFuncionarioDTO.setNome("João Teste");
        novoFuncionarioDTO.setEmail("joao@teste.com");
        novoFuncionarioDTO.setCpf("12345678901");
        novoFuncionarioDTO.setFuncao(FuncaoFuncionario.ADMINISTRATIVO);
        novoFuncionarioDTO.setIdade(30);
        novoFuncionarioDTO.setSenha("123");

        funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setNome("João Teste");
        funcionario.setEmail("joao@teste.com");
        funcionario.setCpf("12345678901");
        funcionario.setFuncao(FuncaoFuncionario.ADMINISTRATIVO);
        funcionario.setIdade(30);
        funcionario.setSenha("123");
    }

    @Test
    @DisplayName("Deve cadastrar um novo funcionário com sucesso")
    void deveCadastrarFuncionarioComSucesso() {
        when(repository.save(any(Funcionario.class))).thenReturn(funcionario);

        FuncionarioDTO resultado = service.cadastrarFuncionario(novoFuncionarioDTO);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getMatricula());
        assertEquals("João Teste", resultado.getNome());

        verify(repository, times(1)).save(any(Funcionario.class));
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao buscar ID que não existe")
    void deveLancarExcecaoAoBuscarIdInexistente() {

        long idInexistente = 99L;

        when(repository.findById(idInexistente)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> {
                    service.buscarFuncionarioPorId(idInexistente);
                }
        );

        assertEquals("Funcionário não encontrado com ID: " + idInexistente, excecao.getMessage());

        verify(repository, times(1)).findById(idInexistente);
    }
}