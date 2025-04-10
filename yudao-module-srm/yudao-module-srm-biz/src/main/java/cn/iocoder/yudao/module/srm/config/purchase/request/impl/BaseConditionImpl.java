package cn.iocoder.yudao.module.srm.config.purchase.request.impl;

import cn.iocoder.yudao.framework.cola.statemachine.Condition;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class BaseConditionImpl implements Condition<T> {

    /**
     * @param context context object
     * @return whether the context satisfied current condition
     */
    @Override
    public boolean isSatisfied(T context) {
        log.debug("BaseConditionImpl isSatisfied:{}", context);
        return true;
    }

    @Override
    public String name() {
        return Condition.super.name();
    }
}
