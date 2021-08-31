package cn.iocoder.yudao.framework.extension.core.context;

import cn.iocoder.yudao.framework.extension.core.BusinessScenario;
import cn.iocoder.yudao.framework.extension.core.point.ExtensionPoint;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @description 执行器通用方法
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-29 00:38
 * @class cn.iocoder.yudao.framework.extension.core.context.AbstractComponentExecutor.java
 */
public abstract class AbstractComponentExecutor {

    /**
     * （"业务" + "用例" + "场景"）执行扩展组件，并返回执行结果
     * @param targetClazz
     * @param businessId
     * @param useCase
     * @param scenario
     * @param function
     * @param <R>
     * @param <T>
     * @return
     */
    public <R, T extends ExtensionPoint> R execute(Class<T> targetClazz, String businessId, String useCase, String scenario, Function<T, R> function) {
        return execute(targetClazz, BusinessScenario.valueOf(businessId, useCase, scenario), function);
    }


    /**
     * （"用例" + "场景"）执行扩展组件，并返回执行结果
     * @param targetClazz
     * @param useCase
     * @param scenario
     * @param function
     * @param <R>
     * @param <T>
     * @return
     */
    public <R, T extends ExtensionPoint> R execute(Class<T> targetClazz, String useCase, String scenario, Function<T, R> function) {
        return execute(targetClazz, BusinessScenario.valueOf(useCase, scenario), function);
    }

    /**
     * （"场景"）执行扩展组件，并返回执行结果
     * @param targetClazz
     * @param scenario
     * @param function
     * @param <R>
     * @param <T>
     * @return
     */
    public <R, T extends ExtensionPoint> R execute(Class<T> targetClazz, String scenario, Function<T, R> function) {
        return execute(targetClazz, BusinessScenario.valueOf(scenario), function);
    }

    /**
     * 执行扩展组件，并返回执行结果
     * @param targetClazz
     * @param businessScenario
     * @param function
     * @param <R> Response Type
     * @param <T> Parameter Type
     * @return
     */
    public <R, T extends ExtensionPoint> R execute(Class<T> targetClazz, BusinessScenario businessScenario, Function<T, R> function) {
        T component = locateComponent(targetClazz, businessScenario);
        return function.apply(component);
    }

    /**
     * （"业务" + "用例" + "场景"）执行扩展组件，适用于无返回值的业务
     * @param targetClazz
     * @param businessId
     * @param useCase
     * @param scenario
     * @param consumer
     * @param <T>
     */
    public <T extends ExtensionPoint> void accept(Class<T> targetClazz, String businessId, String useCase, String scenario, Consumer<T> consumer) {
        accept(targetClazz, BusinessScenario.valueOf(businessId, useCase, scenario), consumer);
    }

    /**
     * （"场景"）执行扩展组件，适用于无返回值的业务
     * @param targetClazz
     * @param useCase
     * @param scenario
     * @param consumer
     * @param <T>
     */
    public <T extends ExtensionPoint> void accept(Class<T> targetClazz, String useCase, String scenario, Consumer<T> consumer) {
        accept(targetClazz, BusinessScenario.valueOf(useCase, scenario), consumer);
    }

    /**
     * （"场景"）执行扩展组件，适用于无返回值的业务
     * @param targetClazz
     * @param scenario
     * @param consumer
     * @param <T>
     */
    public <T extends ExtensionPoint> void accept(Class<T> targetClazz, String scenario, Consumer<T> consumer) {
        accept(targetClazz, BusinessScenario.valueOf(scenario), consumer);
    }

    /**
     * 执行扩展组件，适用于无返回值的业务
     * @param targetClazz
     * @param businessScenario
     * @param consumer
     * @param <T> Parameter Type
     */
    public <T extends ExtensionPoint> void accept(Class<T> targetClazz, BusinessScenario businessScenario, Consumer<T> consumer) {
        T component = locateComponent(targetClazz, businessScenario);
        consumer.accept(component);
    }

    /**
     * 获取/定位扩展点组件
     * @param targetClazz
     * @param businessScenario
     * @param <C>
     * @return
     */
    protected abstract <C extends ExtensionPoint> C locateComponent(Class<C> targetClazz, BusinessScenario businessScenario);
}
