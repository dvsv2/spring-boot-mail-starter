package com.dvsv2.study.tools.mail.services.defaults;

import com.dvsv2.study.tools.mail.MyEmail;
import com.dvsv2.study.tools.mail.services.RecoverMailServer;
import com.dvsv2.study.tools.mail.services.StoreSessionFactory;
import com.sun.mail.pop3.POP3Folder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by liangs on 17/3/31.
 */
public class DefaultRecoverMailServer implements RecoverMailServer {

    public static final Logger LOGGER = LoggerFactory.getLogger(DefaultRecoverMailServer.class);

    private StoreSessionFactory storeSessionFactory;

    private String saveAttachPath = "";

    private List<String> lastUids = new ArrayList<>();
    private Date date = null;

    public DefaultRecoverMailServer(StoreSessionFactory storeSessionFactory, String saveAttachPath) throws ParseException {
        this.storeSessionFactory = storeSessionFactory;
        this.saveAttachPath = saveAttachPath;
//        this.date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//        this.date = sdf.parse("2017-05-22 00:00:00");
        this.date = new Date();
    }

    @Override
    public List<MyEmail> getNewMail() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.date);
        cal.add(Calendar.MINUTE, -2);
        Date relativeDate = cal.getTime();
        ArrayList<MyEmail> result = new ArrayList<MyEmail>();
        Store store = this.storeSessionFactory.getStore();
        Folder messageFolder = store.getFolder("INBOX");
        POP3Folder folder = (POP3Folder) messageFolder;
        folder.open(Folder.READ_WRITE);

        List<Message> messages = getMessages(folder.getMessageCount(), relativeDate, folder);
        LOGGER.info("find new  emails size :" + messages.size());
        ArrayList<Message> goalMessages = new ArrayList<Message>();
        for (Message tmp : messages) {
            goalMessages.add(tmp);
        }
        if (!goalMessages.isEmpty()) {
            result = this.getEmail(goalMessages, folder);
        }
        this.storeSessionFactory.closeOne(store);
        folder.close(true);
        return result;
    }

    private ArrayList<MyEmail> getEmail(ArrayList<Message> messages, POP3Folder folder) throws Exception {
        ArrayList<MyEmail> emails = new ArrayList<MyEmail>();
        for (Message tmpMessage : messages) {
            MimeMessage mimeMessage = (MimeMessage) tmpMessage;
            MyEmail myEmail = new MyEmail();
            myEmail.setBcc(this.getMailAddress("bcc", mimeMessage));
            myEmail.setCc(this.getMailAddress("cc", mimeMessage));
            myEmail.setDate(this.getSentDate(mimeMessage));
            myEmail.setFormadd(this.getFrom(mimeMessage));
            myEmail.setToadd(this.getMailAddress("to", mimeMessage));
            myEmail.setUid(folder.getUID(tmpMessage));
            myEmail.setSubject(this.getSubject(mimeMessage));
            String context = this.getMailContent((Part) tmpMessage);
            myEmail.setFilepath(this.saveAttachMent((Part) tmpMessage));
            myEmail.setContext(context);
            myEmail.setMessage(tmpMessage);
            emails.add(myEmail);
        }
        return emails;
    }


    private String getFrom(MimeMessage mimeMessage) throws Exception {
        InternetAddress from_address[] = (InternetAddress[]) mimeMessage.getFrom();
        String from = from_address[0].getAddress();
        if (from == null)
            from = "";
        return from;
    }

    public String saveAttachMent(Part part) throws Exception {
        String fileName = "";
        StringBuffer path = new StringBuffer("");
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mpart = mp.getBodyPart(i);
                String disposition = mpart.getDisposition();
                if ((disposition != null)
                        && ((disposition.equals(Part.ATTACHMENT)) || (disposition
                        .equals(Part.INLINE)))) {
                    fileName = mpart.getFileName();
                    if (fileName != null) {
                        if (fileName.toLowerCase().indexOf("gb2312") != -1) {
                            fileName = MimeUtility.decodeText(fileName);
                        }
                        saveFile(fileName, mpart.getInputStream());
                        path.append(fileName + "&&&");
                    }
                } else if (mpart.isMimeType("multipart/*")) {
                    saveAttachMent(mpart);
                } else {
                    fileName = mpart.getFileName();
                    if (fileName != null) {
                        fileName = MimeUtility.decodeText(fileName);
                        saveFile(fileName, mpart.getInputStream());
                        path.append(fileName + "&&&");
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachMent((Part) part.getContent());
        }
        return path.toString();
    }

    private String saveFile(String fileName, InputStream in) throws Exception {

        String osName = System.getProperty("os.name");
        String storedir = this.saveAttachPath;
        String separator = "";
        if (osName == null)
            osName = "";
        if (osName.toLowerCase().indexOf("win") != -1) {
            separator = "\\";
            if (storedir == null || storedir.equals(""))
                storedir = "c:\\tmp";
        } else {
            separator = "/";
        }
        String path = storedir + separator + fileName;
        File storefile = new File(path);
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(storefile));
            bis = new BufferedInputStream(in);
            int c;
            while ((c = bis.read()) != -1) {
                bos.write(c);
                bos.flush();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new Exception("文件保存失败!");
        } finally {
            bos.close();
            bis.close();
        }
        return path;
    }

    private String getMailAddress(String type, MimeMessage mimeMessage) throws Exception {
        String mailaddr = "";
        String addtype = type.toUpperCase();
        InternetAddress[] address = null;
        if (addtype.equals("TO") || addtype.equals("CC") || addtype.equals("BCC")) {
            if (addtype.equals("TO")) {
                address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.TO);
            }
            if (addtype.equals("CC")) {
                address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.CC);
            }
            if (addtype.equals("BCC")) {
                address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.BCC);
            }

            if (address != null) {
                for (int i = 0; i < address.length; i++) {
                    String email = address[i].getAddress();
                    if (email == null)
                        email = "";
                    else {
                        email = MimeUtility.decodeText(email);
                    }
                    String personal = address[i].getPersonal();
                    if (personal == null)
                        personal = "";
                    else {
                        personal = MimeUtility.decodeText(personal);
                    }
                    String compositeto = personal + "<" + email + ">";
                    mailaddr += "," + compositeto;
                }
                mailaddr = mailaddr.substring(1);
            }
        } else {
            throw new Exception("mail address type is error");
        }
        return mailaddr;
    }

    private String getSubject(MimeMessage mimeMessage) throws MessagingException {
        String subject = "";
        try {
            subject = MimeUtility.decodeText(mimeMessage.getSubject());
            if (subject == null)
                subject = "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            return subject;
        }
    }

    private String getSentDate(MimeMessage mimeMessage) throws Exception {
        Date date = mimeMessage.getSentDate();
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        return format.format(date);
    }


    public String getMailContent(Part part) throws Exception {
        StringBuffer bodytext = new StringBuffer();
        String contenttype = part.getContentType();
        int nameindex = contenttype.indexOf("name");
        boolean conname = false;
        if (nameindex != -1)
            conname = true;

        if (part.isMimeType("text/plain") && !conname) {
            bodytext.append((String) part.getContent());
        } else if (part.isMimeType("text/html") && !conname) {

            bodytext.append((String) part.getContent());
        } else if (part.isMimeType("multipart/*")) {

            Multipart multipart = (Multipart) part.getContent();
            int counts = multipart.getCount();
            for (int i = 0; i < counts; i++) {
                getMailContent(multipart.getBodyPart(i));
            }
        } else if (part.isMimeType("message/rfc822")) {
            getMailContent((Part) part.getContent());
        }
        return bodytext.toString();
    }


    public List<Message> getMessages(int end, Date date, Folder folder) throws MessagingException {
        int start = end - 9;
        Message[] messages = folder.getMessages(start, end);
        FetchProfile profile = new FetchProfile();
        profile.add(UIDFolder.FetchProfileItem.UID);
        folder.fetch(messages, profile);
        List<Message> result = new ArrayList<>();
        List<Message> msgs = Arrays.asList(messages);
        Collections.reverse(msgs);
        for (Message tmp : msgs) {
            if (match(tmp, date)) {
                result.add(tmp);
            } else {
                return result;
            }
        }
        result.addAll(getMessages(start, date, folder));
        return result;
    }

    public boolean match(Message msg, Date date) throws MessagingException {
        POP3Folder pop3Folder = (POP3Folder) msg.getFolder();
        String uid = "";
        try {
            uid = pop3Folder.getUID(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
        if (date.before(msg.getSentDate()) && !lastUids.contains(uid)) {
            this.lastUids.add(uid);
            return true;
        }
        return false;
    }


}
