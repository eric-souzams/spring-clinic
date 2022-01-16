package com.security.springclinic.service;

import com.security.springclinic.datatables.Datatables;
import com.security.springclinic.datatables.DatatablesColunas;
import com.security.springclinic.exception.AcessoNegadoException;
import com.security.springclinic.model.Perfil;
import com.security.springclinic.model.PerfilTipo;
import com.security.springclinic.model.Usuario;
import com.security.springclinic.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
@AllArgsConstructor
@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final Datatables datatables;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> buscarTodosUsuarios(HttpServletRequest request) {
        datatables.setRequest(request);
        datatables.setColunas(DatatablesColunas.USUARIOS);

        Page<?> page = datatables.getSearch().isEmpty()
                ? usuarioRepository.findAll(datatables.getPageable())
                : usuarioRepository.findByEmailOrPerfil(datatables.getSearch(), datatables.getPageable());

        return datatables.getResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = buscarPorEmailEAtivo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário " + email + " não encontrado."));

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

        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).get();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorIdEPerfis(Long usuarioId, Long[] perfisId) {
        return usuarioRepository.findByIdAndPerfis(usuarioId, perfisId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário e/ou perfil inexistente!"));
    }

    public boolean isSenhaCorreta(String senhaDigitada, String senhaBanco) {
        return encoder.matches(senhaDigitada, senhaBanco);
    }

    @Transactional(readOnly = false)
    public void alterarSenha(Usuario usuario, String senhaDigitada) {
        String novaSenha = encoder.encode(senhaDigitada);
        usuario.setSenha(novaSenha);

        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = false)
    public void salvarCadastroPaciente(Usuario usuario) throws MessagingException {
        String encryptedPassword = encoder.encode(usuario.getSenha());
        usuario.setSenha(encryptedPassword);

        usuario.addPerfil(PerfilTipo.PACIENTE);

        usuarioRepository.save(usuario);

        emailDeConfirmacaoDeCadastro(usuario.getEmail());
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmailEAtivo(String email) {
        return usuarioRepository.findByEmailAndAtivo(email);
    }

    public void emailDeConfirmacaoDeCadastro(String email) throws MessagingException {
        String codigo = Base64Utils.encodeToString(email.getBytes());

        log.info("Codigo de ativacao: {}", codigo);

        emailService.enviarPedidoDeConfirmacaoDeCadastro(email, codigo);
    }

    @Transactional(readOnly = false)
    public void ativarCadastroPaciente(String codigo) {
        String email = new String(Base64Utils.decodeFromString(codigo));

        Usuario usuario = buscarPorEmail(email);
        if (usuario.hasNotId()) {
            throw new AcessoNegadoException("Não foi possivel ativar seu cadastro. Entre em contato com o suporte.");
        }

        usuario.setAtivo(true);
    }

    @Transactional(readOnly = false)
    public void pedidoRedefinicaoDeSenha(String email) throws MessagingException {
        Usuario usuario = buscarPorEmailEAtivo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário " + email + " não encontrado."));

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[4];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String verificador = encoder.encodeToString(bytes);
        log.info("Codigo verificador: {}", verificador);

        usuario.setCodigoVerificador(verificador);

        emailService.enviarPedidoDeRedefinicaoDeSenha(email, verificador);
    }
}
