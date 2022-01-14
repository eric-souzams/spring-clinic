package com.security.springclinic.controller;

import com.security.springclinic.model.Medico;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/medicos")
public class MedicoController {

    @GetMapping(value = "/dados")
    public String abrirPorMedico(Medico medico, ModelMap model) {

        return "medico/cadastro";
    }

}
