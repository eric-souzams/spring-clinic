package com.security.springclinic.controller;

import com.security.springclinic.model.Paciente;
import com.security.springclinic.model.Usuario;
import com.security.springclinic.service.PacienteService;
import com.security.springclinic.service.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@AllArgsConstructor
@Controller
@RequestMapping(value = "/pacientes")
public class PacienteController {

    private final PacienteService service;
    private final UsuarioService usuarioService;

    //pagina de dados pessoais do paciente
    @GetMapping(value = "/dados")
    public String cadastro(Paciente paciente, ModelMap model, @AuthenticationPrincipal User user) {
        paciente = service.buscarPorUsuarioEmail(user.getUsername());

        if (paciente.hasNotId()) {
            paciente.setUsuario(new Usuario(user.getUsername()));
        }

        model.addAttribute("paciente", paciente);

        return "paciente/cadastro";
    }

    //rota para salvar um paciente
    @PostMapping(value = "/salvar")
    public String salvar(Paciente paciente, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        Usuario usuario = usuarioService.buscarPorEmail(user.getUsername());
        if (usuarioService.isSenhaCorreta(paciente.getUsuario().getSenha(), usuario.getSenha())) {
            paciente.setUsuario(usuario);

            service.salvar(paciente);

            redirectAttributes.addFlashAttribute("sucesso", "Seus dados foram inseridos com sucesso.");
        } else {
            redirectAttributes.addFlashAttribute("falha", "Sua senha não confere, tente novamente.");
        }

        return "redirect:/pacientes/dados";
    }

    //rota para editar dados do paciente
    @PostMapping(value = "/editar")
    public String editar(Paciente paciente, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        Usuario usuario = usuarioService.buscarPorEmail(user.getUsername());
        if (usuarioService.isSenhaCorreta(paciente.getUsuario().getSenha(), usuario.getSenha())) {
            service.editar(paciente);

            redirectAttributes.addFlashAttribute("sucesso", "Seus dados foram atualizados com sucesso.");
        } else {
            redirectAttributes.addFlashAttribute("falha", "Sua senha não confere, tente novamente.");
        }

        return "redirect:/pacientes/dados";
    }


}
