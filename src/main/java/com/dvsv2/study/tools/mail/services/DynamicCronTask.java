package com.dvsv2.study.tools.mail.services;

import com.dvsv2.study.tools.mail.MyEmail;
import com.dvsv2.study.tools.mail.annotation.Email;
import com.dvsv2.study.tools.mail.annotation.EmailClient;
import com.dvsv2.study.tools.mail.annotation.EmailFolder;
import com.dvsv2.study.tools.mail.autoconfigure.EmailProperties;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.awt.image.ImageConsumer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by liangs on 17/5/10.
 */
public class DynamicCronTask implements SchedulingConfigurer,ApplicationContextAware {

    private EmailProperties properties;
    private RecoverMailServer recoverMailServer;
    private ApplicationContext applicationContext;

    public static final Logger LOGGER = LoggerFactory.getLogger(DynamicCronTask.class);

    public DynamicCronTask(EmailProperties emailProperties,RecoverMailServer recoverMailServer) {
        this.properties = emailProperties;
        this.recoverMailServer = recoverMailServer;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        List<InvokeHandler> invokeHandlers = ParseInvokeHandler.parse(this.applicationContext);
        taskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
                List<MyEmail> emails = null;
                try {
                    emails = recoverMailServer.getNewMail();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (null != emails && emails.size() > 0) {
                    for (InvokeHandler invokeHandler : invokeHandlers) {
                        for (MyEmail myEmail : emails) {
                            invokeHandler.handle(myEmail);
                        }
                    }
                }
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                if (!Strings.isNullOrEmpty(properties.getCron())) {
                    CronTrigger trigger = new CronTrigger(properties.getCron());
                    return trigger.nextExecutionTime(triggerContext);
                }
                PeriodicTrigger periodicTrigger = new PeriodicTrigger(properties.getInterval(), TimeUnit.SECONDS);
                return periodicTrigger.nextExecutionTime(triggerContext);
            }
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static class ParseInvokeHandler {

        private static List<InvokeHandler> parse(ApplicationContext applicationContext) {
            List<InvokeHandler> notifyMethod = new ArrayList<InvokeHandler>();
            Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(EmailClient.class);
            for (Object obj : beanMap.values()) {
                Method[] methods = obj.getClass().getMethods();
                for (Method method : methods) {
                    EmailFolder emailFolder = AnnotationUtils.findAnnotation(method, EmailFolder.class);
                    if (null != emailFolder) {
                        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                        Class[] parameterTypes = method.getParameterTypes();
                        int i = 0;
                        List<Integer> offsets = new ArrayList<>();
                        List<Object> objects = new ArrayList<>();
                        for (Annotation[] annotations : parameterAnnotations) {
                            Class parameterType = parameterTypes[i++];
                            objects.add(resolveProvidedArgument(parameterType));
                            for (Annotation annotation : annotations) {
                                if (annotation instanceof Email) {
                                    if (parameterType.getName().equals(MyEmail.class.getName())) {
                                        offsets.add(i - 1);
                                    }
                                }
                            }
                        }
                        notifyMethod.add(new InvokeHandler(obj, method, offsets, objects));
                    }
                }
            }
            return notifyMethod;
        }

        private static Object resolveProvidedArgument(Class paramterType) {
            if (paramterType.getName().equals(int.class.getName())) {
                return 0;
            }
            if (paramterType.getName().equals(short.class.getName())) {
                return (byte) 0;
            }
            if (paramterType.getName().equals(byte.class.getName())) {
                return (byte) 0;
            }
            if (paramterType.getName().equals(boolean.class.getName())) {
                return false;
            }
            if (paramterType.getName().equals(float.class.getName())) {
                return 0.0;
            }
            if (paramterType.getName().equals(double.class.getName())) {
                return 0.00;
            }
            if (paramterType.getName().equals(long.class.getName())) {
                return 0L;
            }
            if (paramterType.getName().equals(char.class.getName())) {
                return '0';
            }
            return null;
        }
    }
}
