package cn.fishbar.mail.controller;

import cn.fishbar.mail.MailApplication;
import cn.fishbar.mail.entity.Mail;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import java.io.IOException;


@Component
public class MailReceiver {

    public static final Logger logger = LoggerFactory.getLogger(MailReceiver.class);

    @Autowired
    JavaMailSender mailSender;


    @Autowired
    StringRedisTemplate redisTemplate;

    @RabbitListener(queues = MailApplication.MAIL_QUEUE_NAME)
    public void handler(Message message, Channel channel) throws IOException {
        Mail mail = (Mail) message.getPayload();
        MessageHeaders headers = message.getHeaders();
        Long tag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        String msgId = (String) headers.get("spring_returned_message_correlation");
        if (redisTemplate.opsForHash().entries("mail_log").containsKey(msgId)) {
            //redis 中包含该 key，说明该消息已经被消费过
            logger.info(msgId + ":消息已经被消费");
            channel.basicAck(tag, false);//确认消息已消费
            return;
        }
        //收到消息，发送邮件
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(mail.getFrom());
            mailMessage.setTo(mail.getTo());
            mailMessage.setSubject(mail.getSubject());
            mailMessage.setText(mail.getText());
            mailSender.send(mailMessage);
//            redisTemplate.opsForHash().put("mail_log", msgId, "javaboy");
            channel.basicAck(tag, false);
            logger.info(msgId + ":邮件发送成功");
        } catch (Exception e) {
            channel.basicNack(tag, false, true);
            e.printStackTrace();
            logger.error("邮件发送失败：" + e.getMessage());
        }
    }
}
