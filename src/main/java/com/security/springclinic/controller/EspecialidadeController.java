package com.security.springclinic.controller;

import com.security.springclinic.model.Especialidade;
import com.security.springclinic.service.EspecialidadeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping(value = "/especialidades")
public class EspecialidadeController {

    private final EspecialidadeService service;

    //pagina inicial
    @GetMapping(value = {"", "/"})
    public String especialidades(Especialidade especialidade) {
        return "especialidade/especialidade";
    }

    //salvar espcialidade
    @PostMapping(value = "/salvar")
    public String salvar(Especialidade especialidade, RedirectAttributes redirectAttributes) {
        service.salvar(especialidade);

        redirectAttributes.addFlashAttribute("sucesso", "Operação realizada com sucesso");

        return "redirect:/especialidades";
    }

    //traz todas as especialidades
    @GetMapping(value = "/datatables/server")
    public ResponseEntity<?> getEspecialidades(HttpServletRequest request) {
        return ResponseEntity.ok(service.buscarEspecialidades(request));
    }

    //edita uma especialidade
    @GetMapping(value = "/editar/{id}")
    public String preEditar(@PathVariable("id") Long id, ModelMap model) {
        model.addAttribute("especialidade", service.buscarPorId(id));

        return "especialidade/especialidade";
    }

    //excluir uma especialidade
    @GetMapping(value = "/excluir/{id}")
    public String especialidades(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        service.remover(id);

        redirectAttributes.addFlashAttribute("sucesso", "Operação realizada com sucesso.");

        return "redirect:/especialidades";
    }

    //busca uma especialidade por termo
    @GetMapping(value = "/titulo")
    public ResponseEntity<?> getEspecialidadesPorTermo(@RequestParam("termo") String termo) {
        List<String> especialidades = service.buscarEspecialidadeByTermo(termo);

        return ResponseEntity.ok(especialidades);
    }

    //busca as especialidade de um medico
    @GetMapping(value = "/datatables/server/medico/{id}")
    public ResponseEntity<?> getEspecialidadesPorMedico(@PathVariable("id") Long medicoId, HttpServletRequest request) {
        return ResponseEntity.ok(service.buscarEspecialidadesPorMedico(medicoId, request));
    }

}
