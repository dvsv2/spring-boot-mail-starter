package com.dvsv2.study.tools.mail.annotation;

import java.lang.annotation.*;

/**
 * Created by liangs on 17/5/9.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface Email {
}
