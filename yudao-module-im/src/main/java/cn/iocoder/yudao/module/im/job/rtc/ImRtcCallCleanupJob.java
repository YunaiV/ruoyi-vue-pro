package cn.iocoder.yudao.module.im.job.rtc;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.rtc.ImRtcCallService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 僵尸通话清理 Job：兜底 LiveKit Webhook 丢失 / 客户端异常关闭等未调 leave 的场景
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class ImRtcCallCleanupJob implements JobHandler {

    @Resource
    private ImRtcCallService rtcCallService;

    @Resource
    private ImProperties imProperties;

    /**
     * 执行清理
     *
     * @param param 阈值（分钟）；为空 / 非法走 {@link ImProperties.Rtc#getCleanupZombieThresholdMinutes()} 默认值
     * @return 清理数量描述
     */
    @Override
    @TenantJob
    public String execute(String param) {
        int thresholdMinutes = resolveThresholdMinutes(param);
        int cleaned = rtcCallService.cleanupZombieCalls(thresholdMinutes);
        log.info("[execute][清理僵尸通话数量 ({}) 个]", cleaned);
        return String.format("清理僵尸通话 %s 个", cleaned);
    }

    /**
     * 解析 quartz param 为分钟阈值；非法 / 非正数走配置默认值
     *
     * @param param quartz 调度入参字符串
     * @return 分钟阈值
     */
    private int resolveThresholdMinutes(String param) {
        int defaultMinutes = imProperties.getRtc().getCleanupZombieThresholdMinutes();
        if (StrUtil.isBlank(param) || !NumberUtil.isInteger(param)) {
            return defaultMinutes;
        }
        int minutes = Integer.parseInt(param);
        return minutes > 0 ? minutes : defaultMinutes;
    }

}
