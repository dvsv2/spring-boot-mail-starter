package com.dvsv2.study.tools.mail;

import javax.mail.Message;

/**
 * Created by liangs on 17/3/31.
 */
public class MyEmail {
    private String formadd="";
    private String toadd="";
    private String cc="";
    private String bcc="";
    private String context="";
    private String date="";
    private String subject="";
    private String filepath="";
    private String uid;
    private String id;
    private Message message;

    public String getFormadd() {
        return formadd;
    }

    public void setFormadd(String formadd) {
        this.formadd = formadd;
    }

    public String getToadd() {
        return toadd;
    }

    public void setToadd(String toadd) {
        this.toadd = toadd;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
