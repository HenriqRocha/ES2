package com.example.echo.repository;

import com.example.echo.model.Aluguel;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AluguelRepository extends CrudRepository<Aluguel, Long>{

    //verifica aluguel não finalizado para um ciclista id
    boolean existsByCiclistaIdAndHoraFimIsNull(Long ciclistaId);

    //recupera aluguel ativo se tiver
    Optional<Aluguel> findByCiclistaIdAndHoraFimIsNull(Long ciclistaId);
}
