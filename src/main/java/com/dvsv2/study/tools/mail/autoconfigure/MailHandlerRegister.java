package com.dvsv2.study.tools.mail.autoconfigure;

import com.dvsv2.study.tools.mail.annotation.EmailClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.*;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by liangs on 17/4/1.
 */
@Configuration
public class MailHandlerRegister implements BeanFactoryPostProcessor, ApplicationContextAware,BeanFactoryAware{

    private static final Logger logger = LoggerFactory.getLogger(MailHandlerRegister.class);
    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();
    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();
    private ApplicationContext applicationContext;
    private Set<String> packages = new HashSet<String>();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Scanner scanner = new Scanner((BeanDefinitionRegistry) beanFactory);
        scanner.setResourceLoader(this.applicationContext);
        scanner.scan(StringUtils.toStringArray(packages));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        List<String> packages = AutoConfigurationPackages.get(beanFactory);
        if (logger.isDebugEnabled()) {
            for (String tmp : packages) {
                logger.debug("mail scan package " + tmp);
            }
        }
        this.packages.addAll(packages);
    }

    public final static class Scanner extends ClassPathBeanDefinitionScanner {

        public Scanner(BeanDefinitionRegistry registry) {
            super(registry);
        }

        public void registerDefaultFilters() {
            this.addIncludeFilter(new AnnotationTypeFilter(EmailClient.class));
        }

        public Set<BeanDefinitionHolder> doScan(String... basePackages) {
            Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
            for (BeanDefinitionHolder holder : beanDefinitions) {
                GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
                definition.getPropertyValues().add("innerClassName", definition.getBeanClassName());
                definition.setBeanClass(FactoryBeanTest.class);
            }
            return beanDefinitions;
        }

        public boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return super.isCandidateComponent(beanDefinition) && beanDefinition.getMetadata()
                    .hasAnnotation(EmailClient.class.getName());
        }

    }

    public static class FactoryBeanTest<T> implements InitializingBean, FactoryBean<T> {

        private String innerClassName;

        public void setInnerClassName(String innerClassName) {
            this.innerClassName = innerClassName;
        }

        public T getObject() throws Exception {
            Class innerClass = Class.forName(innerClassName);
            if (innerClass.isInterface()) {
                return (T) InterfaceProxy.newInstance(innerClass);
            } else {
//                Enhancer enhancer = new Enhancer();
//                enhancer.setSuperclass(innerClass);
//                enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
//                enhancer.setCallback(new MethodInterceptorImpl());
                return (T) innerClass.newInstance();
            }
        }


        public Class<?> getObjectType() {
            try {
                return Class.forName(innerClassName);
            } catch (Exception e) {

            }
            return null;
        }

        public boolean isSingleton() {
            return true;
        }

        public void afterPropertiesSet() throws Exception {
            logger.info("init mail client success.");
        }
    }

    public static class InterfaceProxy implements InvocationHandler {

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("ObjectProxy execute:" + method.getName());
            return method.invoke(proxy, args);
        }

        public static <T> T newInstance(Class<T> innerInterface) {
            ClassLoader classLoader = innerInterface.getClassLoader();
            Class[] interfaces = new Class[]{innerInterface};
            InterfaceProxy proxy = new InterfaceProxy();
            return (T) Proxy.newProxyInstance(classLoader, interfaces, proxy);
        }
    }

    public static class MethodInterceptorImpl implements MethodInterceptor {

        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
//            System.out.println("MethodInterceptorImpl:" + method.getName());
            return methodProxy.invokeSuper(o, objects);
        }
    }

}
