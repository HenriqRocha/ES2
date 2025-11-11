package com.example.echo.service;

import com.example.echo.dto.FuncionarioDTO;
import com.example.echo.dto.NovoFuncionarioDTO;
import com.example.echo.model.Funcionario;
import com.example.echo.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.example.echo.exception.RecursoNaoEncontradoException;

@Service
public class FuncionarioService {
    @Autowired
    private FuncionarioRepository repository;

    public FuncionarioDTO cadastrarFuncionario(NovoFuncionarioDTO dto) {
        //validações

        //pega o dto e transforma para a entidade do bd
        Funcionario novoFuncionario = new Funcionario();
        novoFuncionario.setNome(dto.getNome());
        novoFuncionario.setEmail(dto.getEmail());
        novoFuncionario.setCpf(dto.getCpf());
        novoFuncionario.setIdade(dto.getIdade());
        novoFuncionario.setFuncao(dto.getFuncao());
        novoFuncionario.setSenha(dto.getSenha());

        //salvando
        Funcionario funcionarioSalvo = repository.save(novoFuncionario);

        //retorna o funcionario dto salvo
        return new FuncionarioDTO(funcionarioSalvo);
    }

    public FuncionarioDTO buscarFuncionarioPorId(Long id) {
        // Agora, lançamos a nossa própria exceção de negócio
        Funcionario funcionario = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado com ID: " + id));

        return new FuncionarioDTO(funcionario);
    }

    /**
     * (READ) Lógica para listar TODOS os funcionários. (Não muda)
     */
    public List<FuncionarioDTO> listarFuncionarios() {
        Iterable<Funcionario> funcionarios = repository.findAll();
        return StreamSupport.stream(funcionarios.spliterator(), false)
                .map(FuncionarioDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * (UPDATE) Lógica para ATUALIZAR um funcionário.
     * AGORA ESTÁ CORRETO.
     */
    public FuncionarioDTO atualizarFuncionario(Long id, NovoFuncionarioDTO dto) {
        // 1. Verifica se o funcionário existe
        Funcionario funcionarioExistente = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado com ID: " + id));

        // 2. Atualiza os dados
        funcionarioExistente.setNome(dto.getNome());
        funcionarioExistente.setEmail(dto.getEmail());
        funcionarioExistente.setCpf(dto.getCpf());
        funcionarioExistente.setIdade(dto.getIdade());
        funcionarioExistente.setFuncao(dto.getFuncao());
        if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
            funcionarioExistente.setSenha(dto.getSenha());
        }

        // 3. Salva
        Funcionario funcionarioAtualizado = repository.save(funcionarioExistente);
        return new FuncionarioDTO(funcionarioAtualizado);
    }

    /**
     * (DELETE) Lógica para DELETAR um funcionário.
     * AGORA ESTÁ CORRETO.
     */
    public void deletarFuncionario(Long id) {
        // 1. Verifica se o funcionário existe
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Funcionário não encontrado com ID: " + id);
        }

        // 2. Se existir, deleta
        repository.deleteById(id);
    }
}
