package cn.iocoder.yudao.module.im.job.rtc;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.rtc.ImRtcCallService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 振铃超时 Job：扫 INVITING 超过阈值的参与者，单人粒度标 NO_ANSWER + 推 RTC_CALL(REJECT) 让前端 banner 收敛
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class ImRtcParticipantTimeoutJob implements JobHandler {

    @Resource
    private ImRtcCallService rtcCallService;

    @Resource
    private ImProperties imProperties;

    /**
     * 执行超时扫描
     *
     * @param param 阈值（分钟）；为空 / 非法走 {@link ImProperties.Rtc#getInviteTimeoutMinutes()} 默认值
     * @return 超时处理数量描述
     */
    @Override
    @TenantJob
    public String execute(String param) {
        int thresholdMinutes = resolveThresholdMinutes(param);
        int timedOut = rtcCallService.timeoutInvitingParticipants(thresholdMinutes);
        log.info("[execute][振铃超时参与者数量 ({}) 个]", timedOut);
        return String.format("振铃超时 %s 个", timedOut);
    }

    /**
     * 解析 quartz param 为分钟阈值；非法 / 非正数走配置默认值
     *
     * @param param quartz 调度入参字符串
     * @return 分钟阈值
     */
    private int resolveThresholdMinutes(String param) {
        int defaultMinutes = imProperties.getRtc().getInviteTimeoutMinutes();
        if (StrUtil.isBlank(param) || !NumberUtil.isInteger(param)) {
            return defaultMinutes;
        }
        int minutes = Integer.parseInt(param);
        return minutes > 0 ? minutes : defaultMinutes;
    }

}
