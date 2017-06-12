package com.dvsv2.study.tools.mail.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Created by liangs on 17/5/9.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Component
public @interface EmailClient {

}
