package com.mgrecheados.cardapio.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mgrecheados.cardapio.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

}
