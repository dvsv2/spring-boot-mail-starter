package com.dvsv2.study.tools.mail.services.defaults;

import com.dvsv2.study.tools.mail.services.EmailStoreException;
import com.dvsv2.study.tools.mail.services.StoreSessionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.mail.Store;

/**
 * Created by liangs on 17/3/31.
 */
public class DefaultStoreSessionFactory implements StoreSessionFactory {

    private GenericObjectPool<Store> pool;

    public DefaultStoreSessionFactory(StoreSessionPool storeSessionPool) {
        this.pool = new GenericObjectPool<Store>(storeSessionPool);
    }

    @Override
    public Store getStore() {
        try {
            return this.pool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmailStoreException(e.getMessage());
        }
    }

    @Override
    public void closeAll() {
        this.pool.clear();
    }

    @Override
    public void closeOne(Store store) {
        this.pool.returnObject(store);
    }
}
