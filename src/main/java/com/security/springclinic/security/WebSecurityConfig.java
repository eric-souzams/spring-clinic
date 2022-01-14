package com.security.springclinic.security;

import com.security.springclinic.service.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@AllArgsConstructor
@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UsuarioService service;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //acesso publico
                .antMatchers("/webjars/**", "/css/**", "/js/**", "/image/**").permitAll()
                .antMatchers("/", "/home").permitAll()

                //acesso admin
                .antMatchers("/u/**").hasAuthority("ADMIN")

                //acesso medico
                .antMatchers("/medicos/**").hasAuthority("MEDICO")

                //acesso paciente

                .anyRequest().authenticated()

                .and()
                    .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login-error")
                    .permitAll()
                .and()
                    .logout()
                    .logoutSuccessUrl("/");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(service).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
