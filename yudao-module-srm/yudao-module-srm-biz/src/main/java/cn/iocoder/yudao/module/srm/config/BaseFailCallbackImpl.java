package cn.iocoder.yudao.module.srm.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import com.alibaba.cola.statemachine.builder.FailCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.PURCHASE_REQUEST_NOT_EXISTS_BY_STATUS;


//状态机基本异常回调
@Component
@Slf4j
public class BaseFailCallbackImpl<S, E, C> implements FailCallback<S, E, C> {

    public void onFail(S sourceState, E event, C context) {
        //如果sourceState 的类是erpAuditStatus
        String description = convertEventToDescription(sourceState);
        String msg = StrUtil.format("无法在当前状态({})下触发事件({})，上下文：{}", description, SrmEventEnum.valueOf(event.toString()).getDesc(), JSONUtil.toJsonStr(context));
        log.warn(msg);
//        throw new IllegalArgumentException(msg);
//        throw new ServiceException(msg);
        ThrowUtil.ifThrow(true, PURCHASE_REQUEST_NOT_EXISTS_BY_STATUS, description, SrmEventEnum.valueOf(event.toString()).getDesc());
//        throw new TransitionFailException("Cannot fire event [" + event + "] on current state [" + sourceState + "] with context [" + context + "]");
    }

    public String convertEventToDescription(Object event) {
        try {
            // 判断event是否为ArrayValuable的实例
            if (event instanceof ArrayValuable) {
                Method method = event.getClass().getMethod("getDesc");
                return (String) method.invoke(event);
            } else {
                log.warn("Unknown event type: " + event.getClass());
//                throw new IllegalArgumentException("Unknown event type: " + event.getClass());
            }
        } catch (Exception e) {
            log.warn("Error while converting event to description", e);
//            throw new RuntimeException("Error while converting event to description", e);
        }
        return null;
    }
}


