package cn.iocoder.dashboard.framework.tracer.core.annotation;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.util.sping.SpElUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 业务链路AOP切面
 *
 * @author mashu
 */
@Aspect
@Slf4j
public class BizTracingAop {

    @Around(value = "@annotation(bizTracing)")
    public void tagBizInfo(ProceedingJoinPoint joinPoint, BizTracing bizTracing) {
        String bizId = (String) SpElUtil.analysisSpEl(bizTracing.id(), joinPoint);
        String bizType = (String) SpElUtil.analysisSpEl(bizTracing.type(), joinPoint);
        if (StrUtil.isBlankIfStr(bizId)) {
            log.error("empty biz: bizId[{}], bizType[{}].", bizId, bizType);
            return;
        }
        log.info("accept biz: bizId[{}], bizType[{}].", bizId, bizType);
        ActiveSpan.tag(BizTracing.ID_TAG, bizId);
        ActiveSpan.tag(BizTracing.TYPE_TAG, bizType);
    }
}
