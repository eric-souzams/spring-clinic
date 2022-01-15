package com.security.springclinic.service;

import com.security.springclinic.datatables.Datatables;
import com.security.springclinic.datatables.DatatablesColunas;
import com.security.springclinic.model.Especialidade;
import com.security.springclinic.repository.EspecialidadesRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@AllArgsConstructor
@Service
public class EspecialidadeService {

    private final EspecialidadesRepository repository;
    private final Datatables datatables;

    @Transactional(readOnly = false)
    public void salvar(Especialidade especialidade) {
        repository.save(especialidade);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> buscarEspecialidades(HttpServletRequest request) {
        datatables.setRequest(request);
        datatables.setColunas(DatatablesColunas.ESPECIALIDADES);

        Page<?> page = datatables.getSearch().isEmpty()
                ? repository.findAll(datatables.getPageable())
                : repository.findAllByTitulo(datatables.getSearch(), datatables.getPageable());

        return datatables.getResponse(page);
    }

    @Transactional(readOnly = true)
    public Especialidade buscarPorId(Long id) {
        return repository.findById(id).get();
    }

    @Transactional(readOnly = false)
    public void remover(Long id) {
        repository.deleteById(id);
    }

}
