package com.security.springclinic.conversor;

import com.security.springclinic.model.Especialidade;
import com.security.springclinic.service.EspecialidadeService;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Component
public class EspecialidadesConverter implements Converter<String[], Set<Especialidade>> {

    private final EspecialidadeService service;

    @Override
    public Set<Especialidade> convert(String[] titulos) {
        Set<Especialidade> especialidades = new HashSet<>();
        if (titulos != null && titulos.length > 0) {
            especialidades.addAll(service.buscarPorTitulos(titulos));
        }

        return especialidades;
    }
}
