package com.dvsv2.study.tools.mail.services.defaults;

import com.dvsv2.study.tools.mail.MyEmail;
import com.dvsv2.study.tools.mail.services.RecoverMailServer;
import com.dvsv2.study.tools.mail.services.StoreSessionFactory;
import com.google.common.base.Strings;
import com.sun.mail.pop3.POP3Folder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
    private String url = "";
    private List<String> lastUids = new ArrayList<>();
    private Date date = null;

    public DefaultRecoverMailServer(StoreSessionFactory storeSessionFactory, String saveAttachPath, String url) throws ParseException {
        this.storeSessionFactory = storeSessionFactory;
        this.saveAttachPath = saveAttachPath;
        this.url = url;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 2);
        this.date = cal.getTime();
    }

    @Override
    public List<MyEmail> getNewMail() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.date);
        cal.add(Calendar.MINUTE, -2);
        Date relativeDate = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LOGGER.info("current time " + sdf.format(relativeDate));
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
            myEmail.setFilepath(this.saveAttachMent((Part) tmpMessage));
            myEmail.setContext(buildContent(this.getMailContent((Part) tmpMessage)));
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
                    String[] str = mpart.getHeader("Content-Id");
                    if (str != null && str.length > 0) {
                        fileName = str[0].substring(str[0].indexOf("<") + 1, str[0].indexOf(">"));
                    }
                    if (fileName != null) {
                        if (fileName.toLowerCase().indexOf("gb2312") != -1) {
                            fileName = MimeUtility.decodeText(fileName);
                        }
                        saveFile(fileName, mpart.getInputStream());
                        path.append(fileName + "&&&");
                    }
                } else if (mpart.isMimeType("multipart/*")) {
                    path.append(saveAttachMent(mpart));
                } else if (mpart.isMimeType("application/octet-stream")) {
                    fileName = mpart.getFileName();
                    String[] str = mpart.getHeader("Content-Id");
                    if (str != null && str.length > 0) {
                        fileName = str[0].substring(str[0].indexOf("<") + 1, str[0].indexOf(">"));
                    }
                    if (fileName != null) {
                        fileName = MimeUtility.decodeText(fileName);
                        saveFile(fileName, mpart.getInputStream());
                        path.append(fileName + "&&&");
                    }
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
            return saveAttachMent((Part) part.getContent());
        }
        return path.toString();
    }

    public String saveFile(String fileName, InputStream in) throws Exception {
        String osName = System.getProperty("os.name");
        String storedir = this.saveAttachPath;
        if (Strings.isNullOrEmpty(storedir)) {
            return "";
        }
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
        File file = new File(storedir + separator);
        if (!file.exists() && !file.isDirectory()) {
            System.out.println("文件夹不存在新建");
            file.mkdir();
        }
        String path = storedir + separator + fileName + ".jpg";
        File storefile = new File(path);
        if (storefile.exists()) {
            return path;
        }
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }


    public String getMailContent(Part part) throws Exception {
        StringBuffer bodytext = new StringBuffer();//存放邮件内容
        String contenttype = part.getContentType();
        int nameindex = contenttype.indexOf("name");
        boolean conname = false;
        if (nameindex != -1)
            conname = true;

        if (part.isMimeType("text/plain") && !conname) {
            bodytext.append((String) part.getContent());
        } else if (part.isMimeType("text/html") && !conname) {
            String content = (String) part.getContent();
            if (!content.contains("<body")) {
                bodytext.append("<body>");
            }
            bodytext.append((String) part.getContent());
            bodytext.append("</body>");
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int counts = multipart.getCount();
            for (int i = 0; i < counts; i++) {
               bodytext.append(getMailContent(multipart.getBodyPart(i)));
            }
        } else if (part.isMimeType("message/rfc822")) {
            bodytext.append(getMailContent((Part) part.getContent()));
        } else {
        }
        return bodytext.toString();
    }


    public List<Message> getMessages(int end, Date date, Folder folder) throws MessagingException {
        if (end <= 0) {
            return new ArrayList<>();
        }
        int start = end - 9;
        start = start < 1 ? 1 : start;
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
            if (msg.getSentDate().after(this.date)) {
                this.date = msg.getSentDate();
            }
            uid = pop3Folder.getUID(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
        if (date.before(msg.getSentDate()) && !lastUids.contains(uid)) {
            this.lastUids.add(uid);
            if (msg.getSentDate().after(this.date)) {

            }
            return true;
        }
        return false;
    }

    private String buildContent(String str) {
        if (!str.contains("body")) {
            return str;
        }
        Document doc = Jsoup.parse(str);
        Element element = doc.body();
        if (Strings.isNullOrEmpty(this.url) || Strings.isNullOrEmpty(this.saveAttachPath)) {
            return element.html();
        }
        str = str.substring(str.indexOf("<"));
        Elements elements = element.select("img");
        Iterator<Element> iterator = elements.iterator();
        while (iterator.hasNext()) {
            Element tmp = iterator.next();
            Attributes attributes = tmp.attributes();
            if (attributes.hasKey("src")) {
                String src = attributes.get("src");
                String cid = src.substring(src.indexOf("cid:") + 4);
                tmp.removeAttr("src");
                tmp.attr("src", String.format(this.url + "/%s.jpg", cid));
            }
        }
        return element.html();
    }


}
