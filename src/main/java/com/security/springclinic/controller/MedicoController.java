package com.security.springclinic.controller;

import com.security.springclinic.model.Medico;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@AllArgsConstructor
@Controller
@RequestMapping(value = "/medicos")
public class MedicoController {

    private final MedicoService service;
    private final UsuarioService usuarioService;

    @GetMapping(value = "/dados")
    public String abrirPorMedico(Medico medico, ModelMap model, @AuthenticationPrincipal User user) {
        if (medico.hasNotId()) {
            medico = service.buscarPorEmail(user.getUsername());
            model.addAttribute("medico", medico);
        }

        return "medico/cadastro";
    }

    @PostMapping(value = "/salvar")
    public String salvar(Medico medico, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        try {
            if (medico.hasNotId() && medico.getUsuario().hasNotId()) {
                Usuario usuario = usuarioService.buscarPorEmail(user.getUsername());
                medico.setUsuario(usuario);
            }

            service.salvar(medico);

            redirectAttributes.addFlashAttribute("sucesso", "Operação realizada com sucesso.");
            redirectAttributes.addFlashAttribute("medico", medico);
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("falha", "Houve um erro, cadastro não realizado.");
        }

        return "redirect:/medicos/dados";
    }

    @PostMapping(value = "/editar")
    public String editar(Medico medico, RedirectAttributes redirectAttributes) {
        try {
            service.editar(medico);

            redirectAttributes.addFlashAttribute("sucesso", "Operação realizada com sucesso.");
            redirectAttributes.addFlashAttribute("medico", medico);
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("falha", "Houve um erro, cadastro não realizado.");
        }

        return "redirect:/medicos/dados";
    }

    @GetMapping(value = "/id/{idMed}/excluir/especializacao/{idEsp}")
    public String excluirEspecialidadePorMedico(@PathVariable("idMed") Long idMed,
                                       @PathVariable("idEsp") Long idEsp, RedirectAttributes redirectAttributes) {

            if (service.existeEspecialidadeAgendada(idMed, idEsp)) {
                redirectAttributes.addFlashAttribute("falha", "Existem consultas pendentes agendadas, exclusão negada.");
            } else {
                service.excluirEspecialidadePorMedico(idMed, idEsp);

                redirectAttributes.addFlashAttribute("sucesso", "Especialidade removida com sucesso.");
            }

        return "redirect:/medicos/dados";
    }

    @GetMapping(value = "/especialidade/titulo/{titulo}")
    public ResponseEntity<?> getMedicosPorEspecialidade(@PathVariable("titulo") String titulo) {
        return ResponseEntity.ok(service.buscarMedicosPorEspecialidade(titulo));
    }

}
