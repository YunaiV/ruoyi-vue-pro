package cn.iocoder.dashboard.common.annotation;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.util.sping.SpElUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

/**
 * 业务链路AOP切面
 *
 * @author mashu
 */
@Aspect
@Slf4j
@Configuration
public class BizTracingAop {

    @Around(value = "@annotation(bizTracing)")
    public void tagBizInfo(ProceedingJoinPoint joinPoint, BizTracing bizTracing) {
        String bizId = SpElUtil.analysisSpEl(bizTracing.bizId(), joinPoint);
        String bizType = SpElUtil.analysisSpEl(bizTracing.bizType(), joinPoint);
        if (StrUtil.isBlankIfStr(bizId)) {
            log.error("empty biz: bizId[{}], bizType[{}].", bizId, bizType);
            return;
        }
        log.info("accept biz: bizId[{}], bizType[{}].", bizId, bizType);
        ActiveSpan.tag(BizTracing.BIZ_ID_TAG, bizId);
        ActiveSpan.tag(BizTracing.BIZ_TYPE_TAG, bizType);
    }
}
