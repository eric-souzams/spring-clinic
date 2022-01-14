package com.security.springclinic.controller;

import com.security.springclinic.model.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/u")
public class UsuarioController {

    @GetMapping(value = "/novo/cadastro/usuario")
    public String cadastroPorAdminParaAdminMedicoPaciente(Usuario usuario) {
        return "usuario/cadastro";
    }

}
