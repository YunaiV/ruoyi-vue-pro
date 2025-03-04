package cn.iocoder.yudao.module.erp.config.purchase.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import com.alibaba.cola.statemachine.builder.FailCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BaseFailCallbackImpl<S, E, C> implements FailCallback<S, E, C> {

    public void onFail(S sourceState, E event, C context) {
        //如果sourceState 的类是erpAuditStatus
        String msg = StrUtil.format("无法在当前状态({})下触发事件({})，上下文：{}", sourceState, ErpEventEnum.valueOf(event.toString()).getDesc(), JSONUtil.toJsonStr(context));
        log.warn(msg);
        throw new IllegalArgumentException(msg);
//        throw new ServiceException(msg);
//        ThrowUtil.ifThrow(true, PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_STATUS, sourceState, ErpEventEnum.valueOf(event.toString()).getDesc());
//        throw new TransitionFailException("Cannot fire event [" + event + "] on current state [" + sourceState + "] with context [" + context + "]");
    }
}

