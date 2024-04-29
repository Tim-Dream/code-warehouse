package com.dream.mail.util;

import com.dream.mail.config.MailProperties;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.MimeUtility;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

@Component
@AllArgsConstructor
public class ReadMailUtil {

    private MailProperties properties;

    @SneakyThrows
    public void test(){
        Properties prop = new Properties();
        prop.setProperty("mail.store.protocol", "imap");
        prop.setProperty("mail.imap.host", properties.getHost());
        prop.setProperty("mail.imap.port", "143");

        // 创建Session实例对象
        Session session = Session.getInstance(prop);

        // 创建IMAP协议的Store对象
        Store store = session.getStore("imap");

        // 连接邮件服务器
        store.connect(properties.getUsername(), properties.getPassword());

        // 获得收件箱
        Folder folder = store.getFolder("INBOX");
        // 以读写模式打开收件箱
        folder.open(Folder.READ_WRITE);

        // 获得收件箱的邮件列表
        Message[] messages = folder.getMessages();

        // 打印不同状态的邮件数量
        System.out.println("收件箱中共" + messages.length + "封邮件!");
        System.out.println("收件箱中共" + folder.getUnreadMessageCount() + "封未读邮件!");
        System.out.println("收件箱中共" + folder.getNewMessageCount() + "封新邮件!");
        System.out.println("收件箱中共" + folder.getDeletedMessageCount() + "封已删除邮件!");

        System.out.println("------------------------开始解析邮件----------------------------------");


        int total = folder.getMessageCount();
        System.out.println("-----------------您的邮箱共有邮件：" + total + " 封--------------");
        // 得到收件箱文件夹信息，获取邮件列表
        Message[] msgs = folder.getMessages();
        System.out.println("\t收件箱的总邮件数：" + msgs.length);
        for (int i = 0; i < total; i++) {
            Message a = msgs[i];
            //   获取邮箱邮件名字及时间

            StringBuffer content = new StringBuffer(30);
            getMailTextContent(a,content);
            System.out.println("邮件正文："+content);
            Address[] address = a.getReplyTo();
            Address[] from = a.getFrom();

            System.out.println("==============");
//                System.out.println(a.getSubject() + "   接收时间：" + a.getReceivedDate().toLocaleString()+"  contentType()" +a.getContentType());
        }
        System.out.println("\t未读邮件数：" + folder.getUnreadMessageCount());
        System.out.println("\t新邮件数：" + folder.getNewMessageCount());
        System.out.println("----------------End------------------");



        // 关闭资源
        folder.close(false);
        store.close();
    }


    /**
     * 获得邮件文本内容
     *
     * @param part    邮件体
     * @param content 存储邮件文本内容的字符串
     * @throws MessagingException
     */
    @SneakyThrows
    public static void getMailTextContent(Part part, StringBuffer content)  {
        //如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
        boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;
        if (part.isMimeType("text/*") && !isContainTextAttach) {
            content.append(part.getContent().toString());
        } else if (part.isMimeType("message/rfc822")) {
            getMailTextContent((Part) part.getContent(), content);
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailTextContent(bodyPart, content);
            }
        }
    }
}
