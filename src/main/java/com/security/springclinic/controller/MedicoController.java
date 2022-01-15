package com.security.springclinic.controller;

import com.security.springclinic.model.Medico;
import com.security.springclinic.service.MedicoService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@AllArgsConstructor
@Controller
@RequestMapping(value = "/medicos")
public class MedicoController {

    private final MedicoService service;

    @GetMapping(value = "/dados")
    public String abrirPorMedico(Medico medico, ModelMap model) {

        return "medico/cadastro";
    }

    @PostMapping(value = "/salvar")
    public String salvar(Medico medico, RedirectAttributes redirectAttributes) {
        try {
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

}
