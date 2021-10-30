package cn.iocoder.yudao.framework.extension.core.factory;

import cn.iocoder.yudao.framework.extension.core.BusinessScenario;
import cn.iocoder.yudao.framework.extension.core.point.ExtensionPoint;

/**
 * @description 扩展点工厂
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-28 23:04
 * @class cn.iocoder.yudao.framework.extension.core.factory.ExtensionFactory.java
 */
public interface ExtensionFactory {

    /**
     * 注册所有扩展点实现类
     * @param basePackage
     */
    void register(String basePackage);

    /**
     * 根据业务场景获取指定类型的扩展点
     * @param businessScenario
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends ExtensionPoint> T get(BusinessScenario businessScenario, Class<T> clazz);
}
