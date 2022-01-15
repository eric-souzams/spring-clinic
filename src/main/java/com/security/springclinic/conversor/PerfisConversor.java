package com.security.springclinic.conversor;

import com.security.springclinic.model.Perfil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PerfisConversor implements Converter<String[], List<Perfil>> {

    @Override
    public List<Perfil> convert(String[] source) {
        List<Perfil> perfis = new ArrayList<>();
        for (String id : source) {
            if (!id.equals("0")) {
                perfis.add(new Perfil(Long.parseLong(id)));
            }
        }
        return perfis;
    }

}
