package cn.iocoder.yudao.framework.extension.core.context;

import cn.iocoder.yudao.framework.extension.core.BusinessScenario;
import cn.iocoder.yudao.framework.extension.core.factory.ExtensionFactory;
import cn.iocoder.yudao.framework.extension.core.point.ExtensionPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * @description 上下文及扩展点组件工厂的持有类
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-29 00:29
 * @class cn.iocoder.yudao.framework.extension.core.context.ExtensionContextHolder.java
 */
@Component
@Slf4j
public class ExtensionContextHolder implements ExtensionContext{

    @Autowired
    private ExtensionFactory factory;

    @Override
    public <T extends ExtensionPoint> T getPoint(@NotNull String businessId, @NotNull String useCase, @NotNull String scenario, Class<T> clazz) {
        return getPoint(BusinessScenario.valueOf(businessId, useCase, scenario), clazz);
    }

    @Override
    public <T extends ExtensionPoint> T getPoint(@NotNull String useCase, String scenario, Class<T> clazz) {
        return getPoint(BusinessScenario.valueOf(useCase, scenario), clazz);
    }

    @Override
    public <T extends ExtensionPoint> T getPoint(@NotNull String scenario, Class<T> clazz) {
        return getPoint(BusinessScenario.valueOf(scenario), clazz);
    }

    @Override
    public <T extends ExtensionPoint> T getPoint(@NotNull BusinessScenario businessScenario, Class<T> clazz) {
        return factory.get(businessScenario, clazz);
    }
}
