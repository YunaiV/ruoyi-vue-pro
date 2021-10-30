package cn.iocoder.yudao.framework.extension.core.context;

import cn.iocoder.yudao.framework.extension.core.BusinessScenario;
import cn.iocoder.yudao.framework.extension.core.point.ExtensionPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description 扩展组件执行器
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-29 00:32
 * @class cn.iocoder.yudao.framework.extension.core.context.ExtensionExecutor.java
 */
@Component
@Slf4j
public class ExtensionExecutor extends AbstractComponentExecutor{

    @Autowired
    private ExtensionContextHolder contextHolder;


    @Override
    protected <C extends ExtensionPoint> C locateComponent(Class<C> targetClazz, BusinessScenario businessScenario) {
        return contextHolder.getPoint(businessScenario, targetClazz);
    }
}
