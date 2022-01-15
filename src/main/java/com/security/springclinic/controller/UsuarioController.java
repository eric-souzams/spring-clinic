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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping(value = "/u")
public class UsuarioController {

    private final UsuarioService service;
    private final MedicoService medicoService;

    @GetMapping(value = "/novo/cadastro/usuario")
    public String cadastroPorAdminParaAdminMedicoPaciente(Usuario usuario) {
        return "usuario/cadastro";
    }

    @GetMapping(value = "/lista")
    public String listarUsuarios () {
        return "usuario/lista";
    }

    @GetMapping(value = "/datatables/server/usuarios")
    public ResponseEntity<?> listarUsuariosDatatable(HttpServletRequest request) {
        return ResponseEntity.ok(service.buscarTodosUsuarios(request));
    }

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

    @GetMapping(value = "/editar/credenciais/usuario/{id}")
    public ModelAndView preEditarCredenciais(@PathVariable("id") Long id) {
        Usuario usuario = service.buscarPorId(id);

        return new ModelAndView("usuario/cadastro", "usuario", usuario);
    }

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

}
