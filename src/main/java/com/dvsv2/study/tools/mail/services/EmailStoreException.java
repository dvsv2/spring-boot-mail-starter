package com.dvsv2.study.tools.mail.services;

/**
 * Created by liangs on 17/3/31.
 */
public class EmailStoreException extends RuntimeException {
    private static final long serialVersionUID = -5251396252407991334L;

    public EmailStoreException() {
        super();
    }

    public EmailStoreException(String message) {
        super(message);
    }

    public EmailStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailStoreException(Throwable cause) {
        super(cause);
    }
}
