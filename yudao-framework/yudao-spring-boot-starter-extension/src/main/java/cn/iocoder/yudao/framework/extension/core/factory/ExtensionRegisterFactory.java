package cn.iocoder.yudao.framework.extension.core.factory;

import cn.iocoder.yudao.framework.extension.core.BusinessScenario;
import cn.iocoder.yudao.framework.extension.core.point.ExtensionPoint;
import cn.iocoder.yudao.framework.extension.core.stereotype.Extension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description 注册工厂
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-28 23:07
 * @class cn.iocoder.yudao.framework.extension.core.factory.ExtensionRegisterFactory.java
 */
@Component
@Slf4j
public class ExtensionRegisterFactory implements ExtensionFactory {

    /**
     * spring ApplicationContext
     */
    private ApplicationContext applicationContext;

    /**
     * 扩展点实现类集合
     */
    private Map<String, ExtensionDefinition> registerExtensionBeans = new ConcurrentHashMap<>();

    @Override
    public void register(String basePackage) {
        final Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Extension.class);
        if(beans == null || beans.isEmpty()) {
            return;
        }

        beans.values().forEach(point -> doRegister((ExtensionPoint) point));
        log.info("业务场景相关扩展点注册完成，注册数量: {}", registerExtensionBeans.size());
    }

    @Override
    public <T extends ExtensionPoint> T get(BusinessScenario businessScenario, Class<T> clazz) {

        final ExtensionDefinition definition = registerExtensionBeans.get(businessScenario.getUniqueIdentity());
        if(definition == null) {
            log.error("获取业务场景扩展点实现失败，失败原因：尚未定义该业务场景相关扩展点。{}", businessScenario);
            throw new RuntimeException("尚未定义该业务场景相关扩展点 [" + businessScenario + "]");
        }

        return (T) definition.getExtensionPoint();
    }

    /**
     * 注册扩展点
     * @param point
     */
    private void doRegister(@NotNull ExtensionPoint point) {
        Class<?>  extensionClazz = point.getClass();

        if (AopUtils.isAopProxy(point)) {
            extensionClazz = ClassUtils.getUserClass(point);
        }

        Extension extension = AnnotationUtils.findAnnotation(extensionClazz, Extension.class);
        final BusinessScenario businessScenario = BusinessScenario.valueOf(extension.businessId(), extension.useCase(), extension.scenario());
        final ExtensionDefinition definition = ExtensionDefinition.valueOf(businessScenario, point);
        final ExtensionDefinition exist = registerExtensionBeans.get(businessScenario.getUniqueIdentity());
        if(exist != null && !exist.equals(definition)) {
            throw new RuntimeException("相同的业务场景重复注册了不同类型的扩展点实现 :【" + definition + "】【" + exist + "】");
        }

        registerExtensionBeans.put(businessScenario.getUniqueIdentity(), definition);
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
