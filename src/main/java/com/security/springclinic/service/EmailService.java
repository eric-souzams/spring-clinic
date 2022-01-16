package com.security.springclinic.service;

import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@AllArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void enviarPedidoDeConfirmacaoDeCadastro(String destino, String codigo) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

        Context context = new Context();
        context.setVariable("titulo", "Bem-Vindo a clínica Spring Security");
        context.setVariable("texto", "Precisamos que você confirme seu cadastro, clicando no link abaixo.");
        context.setVariable("linkConfirmacao", "http://localhost:8080/u/confirmacao/cadastro?codigo=" + codigo);

        String html = templateEngine.process("email/confirmacao", context);

        helper.setTo(destino);
        helper.setText(html, true);
        helper.setSubject("Confirmação de Cadastro");
        helper.setFrom("nao-responder@admin.com");

        helper.addInline("logo", new ClassPathResource("/static/image/spring-security.png"));

        //descomentar caso queira enviar os emails
        //mailSender.send(message);
    }

    public void enviarPedidoDeRedefinicaoDeSenha(String destino, String verificador) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

        Context context = new Context();
        context.setVariable("titulo", "Redefinição de Senha");
        context.setVariable("texto", "Para redefinir sua senha use o código de verificação abaixo, quando exigido no formulário.");
        context.setVariable("verificador", verificador);

        String html = templateEngine.process("email/confirmacao", context);

        helper.setTo(destino);
        helper.setText(html, true);
        helper.setSubject("Redefinição de Senha");
        helper.setFrom("nao-responder@admin.com");

        helper.addInline("logo", new ClassPathResource("/static/image/spring-security.png"));

        //descomentar caso queira enviar os emails
        //mailSender.send(message);
    }

}
