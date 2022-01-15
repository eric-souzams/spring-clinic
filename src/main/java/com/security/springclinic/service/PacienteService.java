package com.security.springclinic.service;

import com.security.springclinic.model.Paciente;
import com.security.springclinic.repository.PacienteRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class PacienteService {

    private final PacienteRepository repository;

    @Transactional(readOnly = true)
    public Paciente buscarPorUsuarioEmail(String email) {
        return repository.findByUsuarioEmail(email)
                .orElse(new Paciente());
    }

    @Transactional(readOnly = false)
    public void salvar(Paciente paciente) {
        repository.save(paciente);
    }

    @Transactional(readOnly = false)
    public void editar(Paciente paciente) {
        Paciente pac = repository.findById(paciente.getId()).get();

        pac.setNome(paciente.getNome());
        pac.setDtNascimento(paciente.getDtNascimento());
    }
}
