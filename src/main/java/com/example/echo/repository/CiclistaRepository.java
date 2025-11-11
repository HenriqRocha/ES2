package com.example.echo.repository;

import com.example.echo.model.Ciclista;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CiclistaRepository extends CrudRepository<Ciclista, Long> {
    Optional<Ciclista> findByEmail(String email);

    Optional<Ciclista> findByCpf(String cpf);
}
