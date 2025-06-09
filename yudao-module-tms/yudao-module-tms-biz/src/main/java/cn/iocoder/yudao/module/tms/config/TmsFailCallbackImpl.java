package cn.iocoder.yudao.module.tms.config;

import cn.iocoder.yudao.framework.cola.statemachine.builder.FailCallback;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.status.TmsStatusValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.FIRST_MILE_REQUEST_STATUS_MACHINE_ERROR;

/**
 * TMS 状态机失败回调实现类
 */
@Component
@Slf4j
public class TmsFailCallbackImpl<C> implements FailCallback<TmsStatusValue, TmsEventEnum, C> {

    @Override
    public void onFail(TmsStatusValue sourceState, TmsStatusValue targetState, TmsEventEnum event, C context) {
//        String sourceDesc = sourceState.toString();//失效
        String sourceDesc = sourceState.getDesc();
        String eventDesc = event.getDesc();

        String contextName = (context == null) ? "null" : context.getClass().getSimpleName();

        log.warn("【TMS状态机】{} 在状态 [{}] 下无法触发事件 [{}]，上下文类型 [{}]", null, sourceDesc, eventDesc, contextName);

        throw ServiceExceptionUtil.exception(FIRST_MILE_REQUEST_STATUS_MACHINE_ERROR, sourceDesc, eventDesc);
    }

}
