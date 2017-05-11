package com.dvsv2.study.tools.mail.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by liangs on 17/3/31.
 */
@ConfigurationProperties(prefix = EmailProperties.MAIL_PREFIX)
public class EmailProperties {
    static final String MAIL_PREFIX = "email";

    private String host;

    private String username;

    private boolean debug = false;

    private String password;

    private Integer port = 110;

    private Integer timeout;

    private String path;


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public static String getMailPrefix() {
        return MAIL_PREFIX;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
