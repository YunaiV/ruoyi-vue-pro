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
 * 僵尸通话清理 Job：兜底 LiveKit Webhook 丢失 / 客户端异常关闭等未调 leave 等场景
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class ImRtcCallCleanupJob implements JobHandler {

    @Resource
    private ImRtcCallService rtcCallService;

    /**
     * 执行清理
     *
     * @param param 阈值（分钟）；为空走 Service 默认（5 分钟）
     * @return 清理数量描述
     */
    @Override
    @TenantJob
    public String execute(String param) {
        Integer thresholdMinutes = StrUtil.isNotBlank(param) && NumberUtil.isInteger(param)
                ? Integer.parseInt(param) : null;
        int cleaned = rtcCallService.cleanupZombieCalls(thresholdMinutes);
        log.info("[execute][清理僵尸通话数量 ({}) 个]", cleaned);
        return String.format("清理僵尸通话 %s 个", cleaned);
    }

}
