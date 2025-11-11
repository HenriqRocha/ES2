package com.example.echo.repository;

import com.example.echo.model.Aluguel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
public interface AluguelRepository extends CrudRepository<Aluguel, Long>{
}
