package cn.iocoder.yudao.framework.extension.core;

import cn.iocoder.yudao.framework.extension.core.factory.ExtensionRegisterFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;

/**
 * @description 
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-29 00:18
 * @class cn.iocoder.yudao.framework.extension.core.ExtensionBootstrap.java
 */
public class ExtensionBootstrap implements ApplicationContextAware {

    /**
     * spring 容器
     */
    private ApplicationContext applicationContext;

    @Autowired
    private ExtensionRegisterFactory registerFactory;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        registerFactory.setApplicationContext(applicationContext);
        registerFactory.register(null);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
