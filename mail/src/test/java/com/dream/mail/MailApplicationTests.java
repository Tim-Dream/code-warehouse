package com.dream.mail;

import com.dream.mail.util.MailUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MailApplicationTests {

    @Autowired
    private MailUtil qywxMiniUtil;

    @Test
    void contextLoads() {
        qywxMiniUtil.sendSimpleMail("13866131255@163.com","nih","cddfasd");
    }

}
