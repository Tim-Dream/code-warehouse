package com.dream.mail.util;

import com.dream.mail.config.MailProperties;
import freemarker.template.Template;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;


@Component
@AllArgsConstructor
public class SendMailUtil {

    private JavaMailSender mailSender;

    private MailProperties mailProperties;

    private FreeMarkerConfigurer configurer;

    /**
     * 简单文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    public void sendSimpleMail(String to,String subject,String content){
        //创建SimpleMailMessage对象
        SimpleMailMessage message = new SimpleMailMessage();
        //邮件发送人
        message.setFrom(mailProperties.getUsername());
        //邮件接收人
        message.setTo(to);
        //邮件主题
        message.setSubject(subject);
        //邮件内容
        message.setText(content);
        //发送邮件
        mailSender.send(message);
    }

    /**
     * html邮件
     * @param to 收件人,多个时参数形式 ："xxx@xxx.com,xxx@xxx.com,xxx@xxx.com"
     * @param subject 主题
     * @param content 内容
     */
    public void sendHtmlMail(String to, String subject,String content) {
        //获取MimeMessage对象
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper;
        try {
            messageHelper = new MimeMessageHelper(message, true);
            //邮件发送人
            messageHelper.setFrom(mailProperties.getUsername());
            //邮件接收人,设置多个收件人地址
            InternetAddress[] internetAddressTo = InternetAddress.parse(to);
            messageHelper.setTo(internetAddressTo);
            //messageHelper.setTo(to);
            //邮件主题
            message.setSubject(subject);
            //邮件内容，html格式
            messageHelper.setText(content, true);
            //发送
            mailSender.send(message);
            //日志信息
        } catch (Exception ignored) {
        }
    }

    /**
     * 发送模板邮件
     * @param to 接收人
     * @param subject 标题
     * @param templateName 模板名称
     * @param model 模板参数
     * @param receipt 是否添加回执
     */
    @SneakyThrows
    public void sendMessageMail(String to, String subject, String templateName,Map<String,Object> model, boolean receipt){
        MimeMessage message = mailSender.createMimeMessage();
        if(receipt){
            message.setHeader("Disposition-Notification-To","1");
        }
        message.setContentID(System.currentTimeMillis()+"");
        MimeMessageHelper helper = new MimeMessageHelper(message,true);
        helper.setFrom(mailProperties.getUsername());
        helper.setTo(InternetAddress.parse(to));
        helper.setSubject(subject);
        Template template = configurer.getConfiguration().getTemplate(templateName);
        String text = FreeMarkerTemplateUtils.processTemplateIntoString(template,model);
        helper.setText(text,true);
        mailSender.send(message);
    }
}
