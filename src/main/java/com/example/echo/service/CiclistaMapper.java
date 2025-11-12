package com.example.echo.service;

import com.example.echo.dto.CartaoDeCreditoDTO;
import com.example.echo.dto.CiclistaDTO;
import com.example.echo.dto.CiclistaPostDTO;
import com.example.echo.dto.PassaporteDTO;
import com.example.echo.model.CartaoDeCredito;
import com.example.echo.model.Ciclista;
import com.example.echo.model.Nacionalidade;
import org.springframework.stereotype.Service;

@Service
public class CiclistaMapper {

    //trasforma dto de entrada para entidade
    public Ciclista toEntity(CiclistaPostDTO dto) {
        if (dto == null) {
            return null;
        }

        Ciclista ciclista = new Ciclista();
        ciclista.setNome(dto.getNome());
        ciclista.setNascimento(dto.getNascimento());
        ciclista.setNacionalidade(dto.getNacionalidade());
        ciclista.setEmail(dto.getEmail());
        ciclista.setSenha(dto.getSenha());

        //R1
        if (dto.getNacionalidade() == Nacionalidade.BRASILEIRO) {
            ciclista.setCpf(dto.getCpf());
        } else if (dto.getPassaporte() != null) {
            PassaporteDTO passaporteDTO = dto.getPassaporte();
            ciclista.setPassaporteNumero(passaporteDTO.getNumero());
            ciclista.setPassaporteValidade(passaporteDTO.getValidade());
            ciclista.setPassaportePais(passaporteDTO.getPais());
        }

        //mapeia cartao
        if (dto.getMeioDePagamento() != null) {
            ciclista.setCartaoDeCredito(toCartaoEntity(dto.getMeioDePagamento()));
            ciclista.getCartaoDeCredito().setCiclista(ciclista);
        }
        return ciclista;
    }

    //Entidade Ciclista para dto resposta
    public CiclistaDTO toDTO(Ciclista entity){
        if(entity == null){return null;}

        CiclistaDTO dto = new CiclistaDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setNascimento(entity.getNascimento());
        dto.setNacionalidade(entity.getNacionalidade());
        dto.setCpf(entity.getCpf());
        dto.setPassaporte(entity.getPassaporteNumero());
        dto.setEmail(entity.getEmail());
        dto.setStatus(entity.getStatus());

        return dto;
    }

    //converter dto do cartão para entidade cartao
    private CartaoDeCredito toCartaoEntity(CartaoDeCreditoDTO dto){
        if (dto == null) return null;

        CartaoDeCredito entity = new CartaoDeCredito();
        entity.setNomeTitular(dto.getNomeTitular());
        entity.setNumero(dto.getNumero());
        entity.setValidade(dto.getValidade());
        entity.setCvv(dto.getCvv());
        return entity;
    }
}

