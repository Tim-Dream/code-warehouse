package com.dream.mail.model;

import lombok.Data;

@Data
public class MailModel {

    private String id;

    private String email;

    private String subject;

    private String receiveTime;

    private String content;
}
