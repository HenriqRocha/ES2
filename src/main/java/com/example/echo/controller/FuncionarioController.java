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
        return ResponseEntity.ok(funcionarioSalvo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioDTO> buscarPorId(@PathVariable Long id) {
        FuncionarioDTO funcionario = service.buscarFuncionarioPorId(id);
        return ResponseEntity.ok(funcionario);
    }

    /**
     * (READ) Endpoint para listar TODOS os funcionários.
     * GET /funcionario
     */
    @GetMapping
    public ResponseEntity<List<FuncionarioDTO>> listarTodos() {
        List<FuncionarioDTO> funcionarios = service.listarFuncionarios();
        return ResponseEntity.ok(funcionarios);
    }

    /**
     * (UPDATE) Endpoint para ATUALIZAR um funcionário.
     * PUT /funcionario/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody NovoFuncionarioDTO dto
    ) {
        FuncionarioDTO funcionarioAtualizado = service.atualizarFuncionario(id, dto);
        return ResponseEntity.ok(funcionarioAtualizado);
    }

    /**
     * (DELETE) Endpoint para DELETAR um funcionário.
     * DELETE /funcionario/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletarFuncionario(id);

        // Retorna 204 No Content (padrão para DELETE bem sucedido)
        return ResponseEntity.noContent().build();
    }
}
