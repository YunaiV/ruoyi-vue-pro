package cn.iocoder.yudao.framework.extension.core.context;

import cn.iocoder.yudao.framework.extension.core.BusinessScenario;
import cn.iocoder.yudao.framework.extension.core.point.ExtensionPoint;

/**
 * @description 上下文，包含各个扩展点的相关操作
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-28 22:15
 * @class cn.iocoder.yudao.framework.extension.core.context.ExtensionContext.java
 */
public interface ExtensionContext {

    /**
     * 根据业务场景唯一标识获取扩展点组件实现类
     * @param businessId
     * @param useCase
     * @param scenario
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends ExtensionPoint> T getPoint(String businessId, String useCase, String scenario, Class<T> clazz);

    /**
     * 根据（"实例" + "场景"）获取扩展点组件实现类，其中：业务id（businessId）= {@linkplain cn.iocoder.yudao.framework.extension.core.BusinessScenario.DEFAULT_BUSINESS_ID}
     * @param useCase
     * @param scenario
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends ExtensionPoint> T getPoint(String useCase, String scenario, Class<T> clazz);

    /**
     * 根据（"场景"）获取扩展点组件实现类 <br/>
     * 其中：
     *    业务id（businessId）= {@linkplain cn.iocoder.yudao.framework.extension.core.BusinessScenario.DEFAULT_BUSINESS_ID}
     *    实例（useCase）= {@linkplain cn.iocoder.yudao.framework.extension.core.BusinessScenario.DEFAULT_USECASE}
     * @param scenario
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends ExtensionPoint> T getPoint(String scenario, Class<T> clazz);

    /**
     * 根据业务场景唯一标识获取扩展点组件实现类
     * @param businessScenario
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends ExtensionPoint> T getPoint(BusinessScenario businessScenario, Class<T> clazz);
}
