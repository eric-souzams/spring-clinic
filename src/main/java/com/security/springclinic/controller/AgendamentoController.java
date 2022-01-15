package com.security.springclinic.controller;

import com.security.springclinic.model.Agendamento;
import com.security.springclinic.model.Especialidade;
import com.security.springclinic.model.Paciente;
import com.security.springclinic.model.PerfilTipo;
import com.security.springclinic.service.AgendamentoService;
import com.security.springclinic.service.EspecialidadeService;
import com.security.springclinic.service.PacienteService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

@AllArgsConstructor
@Controller
@RequestMapping(value = "/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;
    private final PacienteService pacienteService;
    private final EspecialidadeService especialidadeService;

    @PreAuthorize("hasAuthority('PACIENTE')")
    @GetMapping(value = {"/agendar", "/", ""})
    public String agendarConsulta(Agendamento agendamento) {

        return "agendamento/cadastro";
    }

    @PreAuthorize("hasAnyAuthority('MEDICO','PACIENTE')")
    @GetMapping(value = "/horario/medico/{medicoId}/data/{data}")
    public ResponseEntity<?> getHorarios(@PathVariable("medicoId") Long id,
                                         @PathVariable("data") @DateTimeFormat(iso = ISO.DATE) LocalDate data) {

        return ResponseEntity.ok(service.buscarHorariosNaoAgendadosPorMedicoEData(id, data));
    }

    @PreAuthorize("hasAuthority('PACIENTE')")
    @PostMapping(value = "/salvar")
    public String salvar(Agendamento agendamento, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        Paciente paciente = pacienteService.buscarPorUsuarioEmail(user.getUsername());

        String titulo = agendamento.getEspecialidade().getTitulo();
        Especialidade especialidade = especialidadeService
                .buscarPorTitulos(new String[]{titulo})
                .stream().findFirst().get();

        agendamento.setEspecialidade(especialidade);
        agendamento.setPaciente(paciente);

        service.salvar(agendamento);

        redirectAttributes.addFlashAttribute("sucesso", "Sua consulta foi agendada com sucesso.");

        return "redirect:/agendamentos/agendar";
    }

    @PreAuthorize("hasAnyAuthority('MEDICO','PACIENTE')")
    @GetMapping(value = {"/historico/consultas", "/historico/paciente"})
    public String historico() {

        return "agendamento/historico-paciente";
    }

    @PreAuthorize("hasAnyAuthority('MEDICO','PACIENTE')")
    @GetMapping(value = "/datatables/server/historico")
    public ResponseEntity<?> historicoAgendamentosPorPaciente(HttpServletRequest request,
                                                              @AuthenticationPrincipal User user) {

        if (user.getAuthorities().contains(new SimpleGrantedAuthority(PerfilTipo.PACIENTE.getDesc()))) {
            return ResponseEntity.ok(service.buscarHistoricoPorPacienteEmail(user.getUsername(), request));
        }

        if (user.getAuthorities().contains(new SimpleGrantedAuthority(PerfilTipo.MEDICO.getDesc()))) {
            return ResponseEntity.ok(service.buscarHistoricoPorMedicoEmail(user.getUsername(), request));
        }

        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAnyAuthority('MEDICO','PACIENTE')")
    @GetMapping(value = "/editar/consulta/{id}")
    public String preEditarConsultaPaciente(@PathVariable("id") Long id, ModelMap model,
                                            @AuthenticationPrincipal User user) {

        Agendamento agendamento = service.buscarPorIdEUsuario(id, user.getUsername());
        model.addAttribute("agendamento", agendamento);

        return "agendamento/cadastro";
    }

    @PreAuthorize("hasAnyAuthority('MEDICO','PACIENTE')")
    @PostMapping(value = "/editar")
    public String editarConsulta(Agendamento agendamento, RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal User user) {
        try {
            String titulo = agendamento.getEspecialidade().getTitulo();
            Especialidade especialidade = especialidadeService
                    .buscarPorTitulos(new String[]{titulo})
                    .stream().findFirst().get();

            agendamento.setEspecialidade(especialidade);
            service.editar(agendamento, user.getUsername());

            redirectAttributes.addFlashAttribute("sucesso", "Sua consulta foi alterada com sucesso.");
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("falha", "Houve um erro ao tentar alterar sua consulta.");
        }

        if (user.getAuthorities().contains(new SimpleGrantedAuthority(PerfilTipo.PACIENTE.getDesc()))) {
            return "redirect:/agendamentos/historico/paciente";
        }

        if (user.getAuthorities().contains(new SimpleGrantedAuthority(PerfilTipo.MEDICO.getDesc()))) {
            return "redirect:/agendamentos/historico/consultas";
        }

        return "redirect:/";
    }

    @PreAuthorize("hasAuthority('MEDICO')")
    @GetMapping(value = "/excluir/consulta/{id}")
    public String excluirConsulta(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        service.remover(id);

        redirectAttributes.addFlashAttribute("sucesso", "Consulta excluida com sucesso.");

        return "redirect:/agendamentos/historico/paciente";
    }

}
