package com.security.springclinic.service;

import com.security.springclinic.model.Perfil;
import com.security.springclinic.model.Usuario;
import com.security.springclinic.repository.UsuarioRepositorio;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = buscarPorEmail(email);
        return new User(
                usuario.getEmail(),
                usuario.getSenha(),
                AuthorityUtils.createAuthorityList(getAuthorities(usuario.getPerfis()))
        );
    }

    private String[] getAuthorities(List<Perfil> perfils) {
        String[] authorities = new String[perfils.size()];
        for (int i = 0; i < perfils.size(); i++) {
            authorities[i] = perfils.get(i).getDesc();
        }
        return authorities;
    }
}
