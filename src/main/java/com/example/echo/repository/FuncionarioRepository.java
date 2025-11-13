package com.example.echo.repository;

import com.example.echo.model.Funcionario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface FuncionarioRepository extends CrudRepository<Funcionario, Long>{
    Optional<Funcionario> findByEmail(String email);
}
