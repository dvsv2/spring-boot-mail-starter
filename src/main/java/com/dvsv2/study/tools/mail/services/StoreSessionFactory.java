package com.dvsv2.study.tools.mail.services;

import javax.mail.Store;

/**
 * Created by liangs on 17/3/31.
 */
public interface StoreSessionFactory {
    Store getStore() throws EmailStoreException;

    void closeAll() throws EmailStoreException;

    void closeOne(Store store) throws EmailStoreException;
}
