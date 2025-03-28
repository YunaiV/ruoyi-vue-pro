package cn.iocoder.yudao.module.srm.config.purchase.request.impl;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import com.alibaba.cola.statemachine.Condition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class BaseConditionImpl implements Condition<SrmPurchaseRequestDO> {

    /**
     * @param context context object
     * @return whether the context satisfied current condition
     */
    @Override
    public boolean isSatisfied(SrmPurchaseRequestDO context) {
        log.debug("BaseConditionImpl isSatisfied:{}", context);
        return true;
    }

    @Override
    public String name() {
        return Condition.super.name();
    }
}
