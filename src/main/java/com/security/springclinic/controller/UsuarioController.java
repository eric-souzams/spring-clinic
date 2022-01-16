package com.security.springclinic.controller;

import com.security.springclinic.model.Medico;
import com.security.springclinic.model.Perfil;
import com.security.springclinic.model.PerfilTipo;
import com.security.springclinic.model.Usuario;
import com.security.springclinic.service.MedicoService;
import com.security.springclinic.service.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping(value = "/u")
public class UsuarioController {

    private final UsuarioService service;
    private final MedicoService medicoService;

    //pagina de admin cadastrar usuario
    @GetMapping(value = "/novo/cadastro/usuario")
    public String cadastroPorAdminParaAdminMedicoPaciente(Usuario usuario) {
        return "usuario/cadastro";
    }

    //pagina de admin listar usuarios
    @GetMapping(value = "/lista")
    public String listarUsuarios() {
        return "usuario/lista";
    }

    //rota que traz os usuarios
    @GetMapping(value = "/datatables/server/usuarios")
    public ResponseEntity<?> listarUsuariosDatatable(HttpServletRequest request) {
        return ResponseEntity.ok(service.buscarTodosUsuarios(request));
    }

    //rota de cadastro de novos usuarios
    @PostMapping(value = "/cadastro/salvar")
    public String salvarUsuario(Usuario usuario, RedirectAttributes redirectAttributes) {
        List<Perfil> perfis = usuario.getPerfis();
        if (perfis.size() > 2 || perfis.containsAll(Arrays.asList(new Perfil(PerfilTipo.ADMIN.getCod()), new Perfil(PerfilTipo.PACIENTE.getCod())))
                || perfis.containsAll(Arrays.asList(new Perfil(PerfilTipo.MEDICO.getCod()), new Perfil(PerfilTipo.PACIENTE.getCod())))) {

            redirectAttributes.addFlashAttribute("falha", "Paciente não poder ser Admin /ou Médico.");
            redirectAttributes.addFlashAttribute("usuario", usuario);
        } else {
            try {
                service.salvarUsuario(usuario);
                redirectAttributes.addFlashAttribute("sucesso", "Operação realizada com sucesso");
            } catch (DataIntegrityViolationException e) {
                redirectAttributes.addFlashAttribute("falha", "Cadastro não realizado, email já existente.");
            }
        }

        return "redirect:/u/novo/cadastro/usuario";
    }

    //pagina de edição de dados de usuario
    @GetMapping(value = "/editar/credenciais/usuario/{id}")
    public ModelAndView preEditarCredenciais(@PathVariable("id") Long id) {
        Usuario usuario = service.buscarPorId(id);

        return new ModelAndView("usuario/cadastro", "usuario", usuario);
    }

    //pagina de edição baseada no perfil
    @GetMapping(value = "/editar/dados/usuario/{id}/perfis/{perfis}")
    public ModelAndView preEditarDadosCadastrais(@PathVariable("id") Long usuarioId, @PathVariable("perfis") Long[] perfisId) {
        Usuario usuario = service.buscarPorIdEPerfis(usuarioId, perfisId);

        if (usuario.getPerfis().contains(new Perfil(PerfilTipo.ADMIN.getCod())) &&
                !usuario.getPerfis().contains(new Perfil(PerfilTipo.MEDICO.getCod()))) {

            return new ModelAndView("usuario/cadastro", "usuario", usuario);
        } else if (usuario.getPerfis().contains(new Perfil(PerfilTipo.MEDICO.getCod()))) {

            Medico medico = medicoService.buscarPorUsuarioId(usuarioId);
            return medico.hasNotId()
                    ? new ModelAndView("medico/cadastro", "medico", new Medico(new Usuario(usuarioId)))
                    : new ModelAndView("medico/cadastro", "medico", medico);
        } else if (usuario.getPerfis().contains(new Perfil(PerfilTipo.PACIENTE.getCod()))) {
            ModelAndView model = new ModelAndView("error");

            model.addObject("status", 403);
            model.addObject("error", "Área Restrita");
            model.addObject("message", "Os dados de pacientes são restritos a ele.");

            return model;
        }

        return new ModelAndView("redirect:/u/lista");
    }

    //pagina de edição de senha
    @GetMapping(value = "/editar/senha")
    public String abrirEditarSenha() {
        return "usuario/editar-senha";
    }

    //rota de alteração de senha
    @PostMapping(value = "/confirmar/senha")
    public String editarSenha(@RequestParam("senha1") String s1, @RequestParam("senha2") String s2,
                              @RequestParam("senha3") String s3, @AuthenticationPrincipal User user,
                              RedirectAttributes redirectAttributes) {

        if (!s1.equals(s2)) {
            redirectAttributes.addFlashAttribute("falha", "Senhas não conferem, tente novamente.");
            return "redirect:/u/editar/senha";
        }

        Usuario usuario = service.buscarPorEmail(user.getUsername());
        if (!service.isSenhaCorreta(s3, usuario.getSenha())) {
            redirectAttributes.addFlashAttribute("falha", "Senha atual não confere, tente novamente.");
            return "redirect:/u/editar/senha";
        }

        service.alterarSenha(usuario, s1);

        redirectAttributes.addFlashAttribute("sucesso", "Senha alterada com sucesso.");

        return "redirect:/u/editar/senha";
    }

    //pagina de cadastro de usuario publico
    @GetMapping(value = "/novo/cadastro")
    public String novoCadastro(Usuario usuario) {
        return "cadastrar-se";
    }

    //pagina de sucesso de cadastro de usuario publico
    @GetMapping(value = "/cadastro/realizado")
    public String cadastroRealizado() {
        return "fragments/mensagem";
    }

    //rota de criação de novo usuario
    @PostMapping(value = "/cadastro/paciente/salvar")
    public String salvarCadastroPaciente(Usuario usuario, BindingResult bindingResult) throws MessagingException {
        try {
            service.salvarCadastroPaciente(usuario);
        } catch (DataIntegrityViolationException exception) {
            bindingResult.reject("email", "Ops... Este email já esta sendo utilizado.");

            return "cadastrar-se";
        }

        return "redirect:/u/cadastro/realizado";
    }

    //rota para confirmação de cadastro
    @GetMapping(value = "/confirmacao/cadastro")
    public String respostaConfirmacaoCadastroPaciente(@RequestParam("codigo") String codigo,
                                                      RedirectAttributes redirectAttributes) {

        service.ativarCadastroPaciente(codigo);

        redirectAttributes.addFlashAttribute("alerta", "sucesso");
        redirectAttributes.addFlashAttribute("titulo", "Cadastro Ativado!");
        redirectAttributes.addFlashAttribute("texto", "Parabéns, seu cadastro está ativo.");
        redirectAttributes.addFlashAttribute("subtexto", "Prossiga com seu login/senha");

        return "redirect:/login";
    }

    //pagina de redefinição
    @GetMapping(value = "/p/redefinir/senha")
    public String pedidoRedefinirSenha() {
        return "usuario/pedido-recuperar-senha";
    }

    //form de pedido de recuperação
    @GetMapping(value = "/p/recuperar/senha")
    public String redefinirSenha(String email, ModelMap model) throws MessagingException {
        service.pedidoRedefinicaoDeSenha(email);

        model.addAttribute("sucesso", "Em instantes você receberá um e-mail para prosseguir " +
                "com a redefinição de sua senha.");
        model.addAttribute("usuario", new Usuario(email));

        return "usuario/recuperar-senha";
    }

    //salvar a nova senha da recuperação
    @PostMapping(value = "/p/nova/senha")
    public String confirmacaoDeRedefinicaoDeSenha(Usuario usuario, ModelMap model) {
        Usuario user = service.buscarPorEmail(usuario.getEmail());

        if (!usuario.getCodigoVerificador().equals(user.getCodigoVerificador())) {
            model.addAttribute("falha", "Código verificador não confere.");
            return "usuario/recuperar-senha";
        }

        user.setCodigoVerificador(null);

        service.alterarSenha(user, usuario.getSenha());

        model.addAttribute("alerta", "sucesso");
        model.addAttribute("titulo", "Senha redefinida!");
        model.addAttribute("texto", "Você já pode logar no sistema.");

        return "login";
    }

}
