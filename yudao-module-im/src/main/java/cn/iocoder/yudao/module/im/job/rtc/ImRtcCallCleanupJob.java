package cn.iocoder.yudao.module.im.job.rtc;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.im.service.rtc.ImRtcCallService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 僵尸通话清理 Job：兜底 LiveKit Webhook 丢失 / 客户端异常关闭等未调 leave 的场景
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class ImRtcCallCleanupJob implements JobHandler {

    /**
     * 默认扫描阈值（分钟）；通话创建超过此值仍未结束才纳入扫描，避开「刚发起还在响铃」的合理零人态
     */
    private static final int DEFAULT_THRESHOLD_MINUTES = 5;

    @Resource
    private ImRtcCallService rtcCallService;

    /**
     * 执行清理
     *
     * @param param 阈值（分钟）；为空 / 非法走默认 {@link #DEFAULT_THRESHOLD_MINUTES}
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
     * 解析 quartz param 为分钟阈值；非法 / 非正数走默认值
     */
    private static int resolveThresholdMinutes(String param) {
        if (StrUtil.isBlank(param) || !NumberUtil.isInteger(param)) {
            return DEFAULT_THRESHOLD_MINUTES;
        }
        int minutes = Integer.parseInt(param);
        return minutes > 0 ? minutes : DEFAULT_THRESHOLD_MINUTES;
    }

}
