package cn.iocoder.yudao.module.erp.config.purchase.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cola.statemachine.builder.FailCallback;
import com.alibaba.cola.statemachine.exception.TransitionFailException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@NotNull
@Slf4j
public class BaseFailCallbackImpl<S, E, C> implements FailCallback<S, E, C> {

    public void onFail(S sourceState, E event, C context) {
        String msg = StrUtil.format("无法在当前状态({})下触发事件({})，上下文：{}", sourceState, event, JSONUtil.toJsonStr(context));
        log.warn(msg);
        throw new TransitionFailException(msg);
//        throw new TransitionFailException("Cannot fire event [" + event + "] on current state [" + sourceState + "] with context [" + context + "]");
    }
}

