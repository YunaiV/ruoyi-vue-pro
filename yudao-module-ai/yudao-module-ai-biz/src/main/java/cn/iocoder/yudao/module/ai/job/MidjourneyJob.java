package cn.iocoder.yudao.module.ai.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import org.springframework.stereotype.Component;

/**
 * midjourney job(同步 mj 结果)
 *
 * @author fansili
 * @time 2024/5/28 17:57
 * @since 1.0
 */
@Component
public class MidjourneyJob implements JobHandler {

    @Override
    public String execute(String param) throws Exception {
        // todo @范 同步 midjourney proxy 结果
        return "";
    }
}
