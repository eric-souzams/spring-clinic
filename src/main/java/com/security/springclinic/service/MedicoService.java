package com.security.springclinic.service;

import com.security.springclinic.model.Medico;
import com.security.springclinic.repository.MedicoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class MedicoService {

    private final MedicoRepository repository;

    @Transactional(readOnly = true)
    public Medico buscarPorUsuarioId(Long id) {
        return repository.findByUsuarioId(id)
                .orElse(new Medico());
    }

    @Transactional(readOnly = false)
    public void salvar(Medico medico) {
        repository.save(medico);
    }

    @Transactional(readOnly = false)
    public void editar(Medico medico) {
        Medico med = repository.findById(medico.getId()).get();

        med.setCrm(medico.getCrm());
        med.setDtInscricao(medico.getDtInscricao());
        med.setNome(medico.getNome());

        if (!medico.getEspecialidades().isEmpty()) {
            med.getEspecialidades().addAll(medico.getEspecialidades());
        }
    }
}
