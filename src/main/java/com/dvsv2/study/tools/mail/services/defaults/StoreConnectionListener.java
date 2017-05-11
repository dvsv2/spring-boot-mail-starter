package com.dvsv2.study.tools.mail.services.defaults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;

/**
 * Created by liangs on 17/3/31.
 */
public class StoreConnectionListener implements ConnectionListener{

    private final Logger LOGGER = LoggerFactory.getLogger(StoreConnectionListener.class);

    @Override
    public void opened(ConnectionEvent e) {
        LOGGER.info("email store connected");
    }

    @Override
    public void disconnected(ConnectionEvent e) {
        LOGGER.info("email store disconnected");
    }

    @Override
    public void closed(ConnectionEvent e) {
        LOGGER.info("email store closed");
    }
}
