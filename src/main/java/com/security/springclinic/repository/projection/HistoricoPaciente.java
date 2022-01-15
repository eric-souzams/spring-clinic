package com.security.springclinic.repository.projection;

import com.security.springclinic.model.Especialidade;
import com.security.springclinic.model.Medico;
import com.security.springclinic.model.Paciente;

public interface HistoricoPaciente {

    Long getId();

    Paciente getPaciente();

    String getDataConsulta();

    Medico getMedico();

    Especialidade getEspecialidade();

}
