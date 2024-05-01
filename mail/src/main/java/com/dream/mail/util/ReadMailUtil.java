package com.dream.mail.util;

import com.dream.mail.config.MailProperties;
import com.dream.mail.model.MailModel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
public class ReadMailUtil {

    private MailProperties properties;

    @SneakyThrows
    public List<MailModel> readMail(String lastMailId){
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
        log.info("收件箱中共" + messages.length + "封邮件!");
        log.info("收件箱中共" + folder.getUnreadMessageCount() + "封未读邮件!");
        log.info("收件箱中共" + folder.getNewMessageCount() + "封新邮件!");
        log.info("------------------------开始解析邮件----------------------------------");

        List<MailModel> list = new ArrayList<>();
        // 得到收件箱文件夹信息，获取邮件列表
        for (int i = messages.length-1; i > 0; i--) {
            MimeMessage a = (MimeMessage)messages[i];
            if(StringUtils.hasText(lastMailId) && lastMailId.equals(a.getMessageID())){
                break;
            }
            //   获取邮箱邮件名字及时间
            StringBuffer content = new StringBuffer(30);
            getMailTextContent(a,content);
            MailModel mailModel = new MailModel();
            mailModel.setId(a.getMessageID());
            mailModel.setEmail(getReceiveAddress(a, null));
            mailModel.setSubject(getSubject(a));
            mailModel.setReceiveTime(getSentDate(a,null));
            mailModel.setContent(content.toString());
            list.add(mailModel);
            log.info("==============");
        }
        log.info("----------------End------------------");
        // 关闭资源
        folder.close(false);
        store.close();
        return list;
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


    /**
     * 获得邮件主题
     *
     * @param msg 邮件内容
     * @return 解码后的邮件主题
     */
    @SneakyThrows
    public static String getSubject(MimeMessage msg){
        return MimeUtility.decodeText(msg.getSubject());
    }

    /**
     * 根据收件人类型，获取邮件收件人、抄送和密送地址。如果收件人类型为空，则获得所有的收件人
     * <p>Message.RecipientType.TO  收件人</p>
     * <p>Message.RecipientType.CC  抄送</p>
     * <p>Message.RecipientType.BCC 密送</p>
     *
     * @param msg  邮件内容
     * @param type 收件人类型
     * @return  邮件地址1,邮件地址2, ...
     */
    @SneakyThrows
    public static String getReceiveAddress(MimeMessage msg, Message.RecipientType type) {
        StringBuffer receiveAddress = new StringBuffer();
        Address[] addresss = null;
        if (type == null) {
            addresss = msg.getAllRecipients();
        } else {
            addresss = msg.getRecipients(type);
        }

        if (addresss == null || addresss.length < 1)
            throw new MessagingException("没有收件人!");
        for (Address address : addresss) {
            InternetAddress internetAddress = (InternetAddress) address;
            receiveAddress.append(internetAddress.getAddress()).append(",");
        }

        receiveAddress.deleteCharAt(receiveAddress.length() - 1); //删除最后一个逗号

        return receiveAddress.toString();
    }

    /**
     * 获得邮件发送时间
     *
     * @param msg 邮件内容
     * @return yyyy年mm月dd日 星期X HH:mm
     */
    @SneakyThrows
    public static String getSentDate(MimeMessage msg, String pattern) {
        Date receivedDate = msg.getSentDate();
        if (receivedDate == null)
            return "";

        if (pattern == null || "".equals(pattern))
            pattern = "yyyy-MM-dd HH:mm:ss ";

        return new SimpleDateFormat(pattern).format(receivedDate);
    }
}
