package cn.iocoder.dashboard.util;

import cn.hutool.core.bean.BeanUtil;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;

/**
 * http://www.bubuko.com/infodetail-3471885.html
 */
public class AopTargetUtils {

    /**
     * 获取 目标对象
     *
     * @param proxy 代理对象
     * @return
     * @throws Exception
     */
    public static Object getTarget(Object proxy) throws Exception {
        if (!AopUtils.isAopProxy(proxy)) {
            return proxy; //不是代理对象
        }
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            return getJdkDynamicProxyTargetObject(proxy);
        } else { //cglib
            return getCglibProxyTargetObject(proxy);
        }
    }

    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Object dynamicAdvisedInterceptor = BeanUtil.getFieldValue(proxy, "CGLIB$CALLBACK_0");
        AdvisedSupport advisedSupport = (AdvisedSupport) BeanUtil.getFieldValue(dynamicAdvisedInterceptor, "advised");
        Object target = advisedSupport.getTargetSource().getTarget();
        return target;
    }

    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        AopProxy aopProxy = (AopProxy) BeanUtil.getFieldValue(proxy, "h");
        AdvisedSupport advisedSupport = (AdvisedSupport) BeanUtil.getFieldValue(aopProxy, "advised");
        Object target = advisedSupport.getTargetSource().getTarget();
        return target;
    }

}