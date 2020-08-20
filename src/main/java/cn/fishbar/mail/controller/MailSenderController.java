package cn.fishbar.mail.controller;

import cn.fishbar.mail.MailApplication;
import cn.fishbar.mail.entity.Mail;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailSenderController {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/test")
    public String sendMail(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("n65785828@163.com");
        message.setTo("747139588@qq.com");
        message.setSubject("测试邮件");
        message.setText("测试邮件内容");
        mailSender.send(message);
        return "success";
    }

    @GetMapping("/test1")
    public String sendMailQueue(){
        Mail mail = new Mail();
        mail.setFrom("n65785828@163.com");
        mail.setTo("747139588@qq.com");
        mail.setSubject("hello");
        mail.setText("hello world");
        rabbitTemplate.convertAndSend(MailApplication.MAIL_EXCHANGE_NAME, MailApplication.MAIL_ROUTING_KEY_NAME,mail,new CorrelationData(mail.toString()));
        return "success";
    }
}
