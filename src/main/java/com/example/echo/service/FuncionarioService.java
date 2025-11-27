package com.example.echo.service;

import com.example.echo.dto.FuncionarioDTO;
import com.example.echo.dto.NovoFuncionarioDTO;
import com.example.echo.exception.DadosInvalidosException;
import com.example.echo.model.Funcionario;
import java.util.Optional;

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

        //dados 'comuns'
        atualizarDadosBase(novoFuncionario, dto);

        //senha
        novoFuncionario.setSenha(dto.getSenha());

        //salvando
        Funcionario funcionarioSalvo = repository.save(novoFuncionario);

        //retorna o funcionario dto salvo
        return new FuncionarioDTO(funcionarioSalvo);
    }

    public FuncionarioDTO buscarFuncionarioPorId(Long id) {
        Funcionario funcionario = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado com ID: " + id));

        return new FuncionarioDTO(funcionario);
    }


    public List<FuncionarioDTO> listarFuncionarios() {
        Iterable<Funcionario> funcionarios = repository.findAll();
        return StreamSupport.stream(funcionarios.spliterator(), false)
                .map(FuncionarioDTO::new)
                .collect(Collectors.toList());
    }


    public FuncionarioDTO atualizarFuncionario(Long id, NovoFuncionarioDTO dto) {
        //funcionario existe?
        Funcionario funcionarioExistente = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado com ID: " + id));

        //email ja usado
        Optional<Funcionario> funcionarioComEmail = repository.findByEmail(dto.getEmail());
        if (funcionarioComEmail.isPresent() && !funcionarioComEmail.get().getId().equals(id)) {
            throw new DadosInvalidosException("Email já cadastrado por outro funcionário.");
        }

        funcionarioExistente.setNome(dto.getNome());
        funcionarioExistente.setEmail(dto.getEmail());
        funcionarioExistente.setIdade(dto.getIdade());
        funcionarioExistente.setFuncao(dto.getFuncao());

        if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
            funcionarioExistente.setSenha(dto.getSenha());
        }

        // Salva
        Funcionario funcionarioAtualizado = repository.save(funcionarioExistente);
        return new FuncionarioDTO(funcionarioAtualizado);
    }


    public void deletarFuncionario(Long id) {
        //funcionario existe?
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Funcionário não encontrado com ID: " + id);
        }

        repository.deleteById(id);
    }

    //Métodos auxiliares

    private void atualizarDadosBase(Funcionario funcionario, NovoFuncionarioDTO dto) {
        funcionario.setNome(dto.getNome());
        funcionario.setEmail(dto.getEmail());
        funcionario.setCpf(dto.getCpf());
        funcionario.setIdade(dto.getIdade());
        funcionario.setFuncao(dto.getFuncao());
    }
}
