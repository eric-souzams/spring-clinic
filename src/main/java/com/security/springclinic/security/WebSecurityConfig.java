package com.security.springclinic.security;

import com.security.springclinic.model.PerfilTipo;
import com.security.springclinic.service.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@AllArgsConstructor
@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String ADMIN = PerfilTipo.ADMIN.getDesc();
    private static final String MEDICO = PerfilTipo.MEDICO.getDesc();
    private static final String PACIENTE = PerfilTipo.PACIENTE.getDesc();

    private final UsuarioService service;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //acesso publico
                .antMatchers("/webjars/**", "/css/**", "/js/**", "/image/**").permitAll()
                .antMatchers("/", "/home", "/expired").permitAll()
                .antMatchers("/u/novo/cadastro", "/u/cadastro/realizado", "/u/cadastro/paciente/salvar").permitAll()
                .antMatchers("/u/confirmacao/cadastro").permitAll()
                .antMatchers("/u/p/**", "/u/p/redefinir/senha", "/u/p/recuperar/senha", "/u/p/nova/senha").permitAll()

                //acesso admin
                .antMatchers("/u/editar/senha", "/u/confirmar/senha").hasAnyAuthority(MEDICO, PACIENTE)
                .antMatchers("/u/**").hasAuthority(ADMIN)

                //acesso medico
                .antMatchers("/medicos/especialidade/titulo/*").hasAnyAuthority(PACIENTE, MEDICO)
                .antMatchers("/medicos/dados", "/medicos/salvar", "/medicos/editar").hasAnyAuthority(MEDICO, ADMIN)
                .antMatchers("/medicos/**").hasAuthority(MEDICO)

                //acesso especialidades
                .antMatchers("/especialidades/titulo").hasAnyAuthority(MEDICO, ADMIN, PACIENTE)
                .antMatchers("/especialidades/datatables/server/medico/*").hasAnyAuthority(MEDICO, ADMIN)
                .antMatchers("/especialidades/datatables/server").hasAnyAuthority(ADMIN)
                .antMatchers("/especialidades/editar/*").hasAnyAuthority(ADMIN)
                .antMatchers("/especialidades/salvar").hasAnyAuthority(ADMIN)
                .antMatchers("/especialidades/excluir/*").hasAnyAuthority(ADMIN)
                .antMatchers("/especialidades/**").hasAuthority(ADMIN)

                //acesso agendamentos
                .antMatchers("/agendamentos/editar").hasAnyAuthority(PACIENTE, MEDICO)
                .antMatchers("/agendamentos/editar/consulta/*").hasAnyAuthority(PACIENTE, MEDICO)
                .antMatchers("/agendamentos/datatables/server/historico").hasAnyAuthority(PACIENTE, MEDICO)
                .antMatchers("/agendamentos/horario/medico/*/data/*").hasAnyAuthority(PACIENTE, MEDICO)
                .antMatchers("/agendamentos/excluir/consulta/*").hasAnyAuthority(PACIENTE, MEDICO)

                .antMatchers("/agendamentos/historico/consultas").hasAuthority(MEDICO)

                .antMatchers("/agendamentos/agendar").hasAnyAuthority(PACIENTE)
                .antMatchers("/agendamentos/historico/paciente").hasAuthority(PACIENTE)
                .antMatchers("/agendamentos/salvar").hasAuthority(PACIENTE)
                .antMatchers("/agendamentos/**").hasAuthority(PACIENTE)

                //acesso paciente
                .antMatchers("/pacientes/**").hasAuthority(PACIENTE)

                .anyRequest().authenticated()
                .and()
                    .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login-error")
                    .permitAll()
                .and()
                    .logout()
                    .logoutSuccessUrl("/")
                    .deleteCookies("JSESSIONID")
                .and()
                    .exceptionHandling()
                    .accessDeniedPage("/acesso-negado")
                .and()
                    .rememberMe();

        http.sessionManagement()
                .maximumSessions(1)
                .expiredUrl("/expired")
                .maxSessionsPreventsLogin(false)
                .sessionRegistry(sessionRegistry());

        http.sessionManagement()
                .sessionFixation().newSession()
                .sessionAuthenticationStrategy(sessionAuthenticationStrategy());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(service).passwordEncoder(passwordEncoder);
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(sessionRegistry());
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public ServletListenerRegistrationBean<?> servletRegistrationBean() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }

}
