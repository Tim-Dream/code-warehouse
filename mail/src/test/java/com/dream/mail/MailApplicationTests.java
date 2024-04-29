package com.dream.mail;

import com.dream.mail.util.ReadMailUtil;
import com.dream.mail.util.SendMailUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class MailApplicationTests {

    @Autowired
    private SendMailUtil sendMailUtil;

    @Autowired
    private ReadMailUtil readMailUtil;

    @Test
    void sendSimpleMail() {
        sendMailUtil.sendSimpleMail("13866131255@163.com","nih","你好，这是一封邮件");
    }

    @Test
    void sendHtmlMail() {
        sendMailUtil.sendHtmlMail("13866131255@163.com","nih","<h1>你好，这是一封邮件</h1>");
    }

    @Test
    void sendMessageMail() {
        Map<String,Object> map = new HashMap<>();
        map.put("messageCode","messageCode");
        map.put("messageStatus","messageStatus");
        map.put("cause","cause");
        sendMailUtil.sendMessageMail("13866131255@163.com","尊敬的用户", "mail.ftl",map,true);
    }

    @Test
    void test() {
        readMailUtil.test();
    }

}
