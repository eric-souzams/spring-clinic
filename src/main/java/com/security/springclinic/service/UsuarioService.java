package com.security.springclinic.service;

import com.security.springclinic.datatables.Datatables;
import com.security.springclinic.datatables.DatatablesColunas;
import com.security.springclinic.model.Perfil;
import com.security.springclinic.model.Usuario;
import com.security.springclinic.repository.UsuarioRepositorio;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final Datatables datatables;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> buscarTodosUsuarios(HttpServletRequest request) {
        datatables.setRequest(request);
        datatables.setColunas(DatatablesColunas.USUARIOS);

        Page<?> page = datatables.getSearch().isEmpty()
                ? usuarioRepositorio.findAll(datatables.getPageable())
                : usuarioRepositorio.findByEmailOrPerfil(datatables.getSearch(), datatables.getPageable());

        return datatables.getResponse(page);
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

    @Transactional(readOnly = false)
    public void salvarUsuario(Usuario usuario) {
        String encryptedPassword = encoder.encode(usuario.getSenha());
        usuario.setSenha(encryptedPassword);

        usuarioRepositorio.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepositorio.findById(id).get();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorIdEPerfis(Long usuarioId, Long[] perfisId) {
        return usuarioRepositorio.findByIdAndPerfis(usuarioId, perfisId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário e/ou perfil inexistente!"));
    }

    public boolean isSenhaCorreta(String senhaDigitada, String senhaBanco) {
        return encoder.matches(senhaDigitada, senhaBanco);
    }

    @Transactional(readOnly = false)
    public void alterarSenha(Usuario usuario, String senhaDigitada) {
        String novaSenha = encoder.encode(senhaDigitada);
        usuario.setSenha(novaSenha);

        usuarioRepositorio.save(usuario);
    }
}
