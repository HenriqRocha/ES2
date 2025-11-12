package com.example.echo.controller;

import com.example.echo.dto.FuncionarioDTO;
import com.example.echo.dto.NovoFuncionarioDTO;
import com.example.echo.service.FuncionarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/funcionario")
public class FuncionarioController {

    @Autowired
    private FuncionarioService service;

    @PostMapping
    public ResponseEntity<FuncionarioDTO> cadastrar(
           @Valid @RequestBody NovoFuncionarioDTO dto
    ) {
        FuncionarioDTO funcionarioSalvo = service.cadastrarFuncionario(dto);
        return ResponseEntity.ok(funcionarioSalvo);//200
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioDTO> buscarPorId(@PathVariable Long id) {
        FuncionarioDTO funcionario = service.buscarFuncionarioPorId(id);
        return ResponseEntity.ok(funcionario);//200
    }

    //listar funcionarios
    @GetMapping
    public ResponseEntity<List<FuncionarioDTO>> listarTodos() {
        List<FuncionarioDTO> funcionarios = service.listarFuncionarios();
        return ResponseEntity.ok(funcionarios);//200
    }

    //atualiza funcionario
    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody NovoFuncionarioDTO dto
    ) {
        FuncionarioDTO funcionarioAtualizado = service.atualizarFuncionario(id, dto);
        return ResponseEntity.ok(funcionarioAtualizado);//200
    }

    //deletar funcionario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletarFuncionario(id);

        //200
        return ResponseEntity.ok().build();
    }
}
