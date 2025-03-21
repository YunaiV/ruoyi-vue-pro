package cn.iocoder.yudao.module.erp.config.purchase.request.impl;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import com.alibaba.cola.statemachine.Condition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class BaseConditionImpl implements Condition<ErpPurchaseRequestDO> {

    /**
     * @param context context object
     * @return whether the context satisfied current condition
     */
    @Override
    public boolean isSatisfied(ErpPurchaseRequestDO context) {
        log.debug("BaseConditionImpl isSatisfied:{}", context);
        return true;
    }

    @Override
    public String name() {
        return Condition.super.name();
    }
}
