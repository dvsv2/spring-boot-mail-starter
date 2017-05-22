package com.dvsv2.study.tools.mail.services.defaults;

import com.dvsv2.study.tools.mail.autoconfigure.EmailProperties;
import com.sun.mail.util.MailSSLSocketFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import java.security.Security;
import java.util.Properties;

/**
 * Created by liangs on 17/3/31.
 */
public class StoreSessionPool implements PooledObjectFactory<Store> {

    private EmailProperties properties;

    public StoreSessionPool(EmailProperties emailProperties) {
        this.properties = emailProperties;
    }

    @Override
    public PooledObject<Store> makeObject() throws Exception {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        Properties props = System.getProperties();
        props.put("mail.pop3.ssl.trust", "*");
        props.put("mail.pop3.ssl.socketFactory", sf);
        props.put("mail.pop3.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.pop3.host", this.properties.getHost());
        props.put("mail.store.protocol", "pop3");
        props.put("mail.pop3.auth", "true");
        props.put("mail.pop3.socketFactory.port", this.properties.getPort());
        props.put("mail.pop3.port", this.properties.getPort());
        props.put("mail.pop3.connectiontimeout", this.properties.getTimeout() * 1000);
        props.put("mail.pop3.timeout", this.properties.getTimeout() * 1000);
        Session session = Session.getDefaultInstance(props, null);
        URLName urlName = new URLName("pop3", this.properties.getHost(),
                this.properties.getPort(), null, this.properties.getUsername(), this.properties.getPassword());
        Store store = session.getStore(urlName);
        session.setDebug(this.properties.isDebug());
        store.addConnectionListener(new StoreConnectionListener());
        store.connect();
        PooledObject<Store> poolObject = new DefaultPooledObject(store);
        return poolObject;
    }

    @Override
    public void destroyObject(PooledObject<Store> pooledObject) throws Exception {
        Store store = pooledObject.getObject();
        if (store.isConnected()) {
            store.close();
        }
    }

    @Override
    public boolean validateObject(PooledObject<Store> pooledObject) {
        Store store = pooledObject.getObject();
        return store.isConnected();
    }

    @Override
    public void activateObject(PooledObject<Store> pooledObject) throws Exception {
        Store store = pooledObject.getObject();
        if (!store.isConnected()) {
            store.connect();
        }
    }

    @Override
    public void passivateObject(PooledObject<Store> pooledObject) throws Exception {

    }
}
