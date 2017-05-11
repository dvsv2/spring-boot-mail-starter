package com.dvsv2.study.tools.mail.services;


import com.dvsv2.study.tools.mail.MyEmail;

import java.util.List;

/**
 * Created by liangs on 17/3/31.
 */
public interface RecoverMailServer {

    List<MyEmail> getNewMail() throws Exception;

}
