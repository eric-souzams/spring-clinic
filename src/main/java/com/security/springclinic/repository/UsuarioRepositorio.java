package com.security.springclinic.repository;

import com.security.springclinic.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    @Query("select u from Usuario u where u.email like :email")
    Usuario findByEmail(@Param("email") String email);

}
