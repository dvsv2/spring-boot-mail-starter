package com.dvsv2.study.tools.mail.autoconfigure;

import com.dvsv2.study.tools.mail.services.DynamicCronTask;
import com.dvsv2.study.tools.mail.services.StoreSessionFactory;
import com.dvsv2.study.tools.mail.services.defaults.DefaultStoreSessionFactory;
import com.dvsv2.study.tools.mail.services.defaults.StoreSessionPool;
import com.dvsv2.study.tools.mail.services.RecoverMailServer;
import com.dvsv2.study.tools.mail.services.defaults.DefaultRecoverMailServer;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.List;


/**
 * Created by liangs on 17/3/31.
 */
@Configuration
@ConditionalOnClass({StoreSessionFactory.class, RecoverMailServer.class})
@EnableConfigurationProperties(EmailProperties.class)
@EnableScheduling
public class EmailAutoConfiguration {

    private static Logger logger = LoggerFactory.getLogger(EmailAutoConfiguration.class);

    @Autowired
    private EmailProperties properties;

    @PostConstruct
    public void checkConfig() {
        Assert.state(!Strings.isNullOrEmpty(this.properties.getHost()), "can not find config email.host");
        Assert.state(!Strings.isNullOrEmpty(this.properties.getPassword()), "can not find config email.password");
        Assert.state(!Strings.isNullOrEmpty(this.properties.getUsername()), "can not find config email.username");
    }

    @Bean
    @ConditionalOnMissingBean
    public StoreSessionFactory storeSessionFactory() throws Exception {
        logger.info("create default StoreSessionFactory bean");
        StoreSessionPool storeSessionPool = new StoreSessionPool(this.properties);
        DefaultStoreSessionFactory storeSessionFactory = new DefaultStoreSessionFactory(storeSessionPool);
        return storeSessionFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public RecoverMailServer recoverMailServer(StoreSessionFactory storeSessionFactory) throws ParseException {
        logger.info("create default RecoverMailServer bean");
        return new DefaultRecoverMailServer(storeSessionFactory, (this.properties.getPath()));
    }


    @Bean
    public DynamicCronTask dynamicCronTask(RecoverMailServer recoverMailServer) {
        return new DynamicCronTask(properties, recoverMailServer);
    }




}
