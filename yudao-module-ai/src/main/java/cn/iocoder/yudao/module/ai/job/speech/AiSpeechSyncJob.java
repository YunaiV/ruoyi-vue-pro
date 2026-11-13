package cn.iocoder.yudao.module.ai.job.speech;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.ai.service.speech.AiSpeechService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI 语音同步 Job：查询异步任务（腾讯云）的结果，并把超时的语音置为已失败
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class AiSpeechSyncJob implements JobHandler {

    @Resource
    private AiSpeechService speechService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = speechService.syncSpeech();
        log.info("[execute][同步 AI 语音 ({}) 个]", count);
        return String.format("同步 AI 语音 %s 个", count);
    }

}
