package com.dvsv2.study.tools.mail;

import com.dvsv2.study.tools.mail.annotation.Email;
import com.dvsv2.study.tools.mail.annotation.EmailFolder;

/**
 * Created by liangs on 17/5/10.
 */
@com.dvsv2.study.tools.mail.annotation.EmailClient
public class EmailClient {
    @EmailFolder
    public void handler(@Email MyEmail test, String test1, long test3) {
        System.out.println(test.getSubject());
    }
}
