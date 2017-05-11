package com.dvsv2.study.tools.mail.services.defaults;

import com.dvsv2.study.tools.mail.autoconfigure.EmailProperties;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
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
        Properties properties = new Properties();
        properties.setProperty("mail.pop3.host", this.properties.getHost());
        properties.setProperty("mail.store.protocol", "pop3");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.timeout", "1000");
        Session session = Session.getDefaultInstance(properties);
        URLName urlName = new URLName("pop3", this.properties.getHost(),
                this.properties.getPort(), null, this.properties.getUsername(), this.properties.getPassword());
        Store store = session.getStore(urlName);
        session.setDebug(this.properties.isDebug());
        store.addConnectionListener(new StoreConnectionListener());
        PooledObject<Store> poolObject = new DefaultPooledObject(Store.class);
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
