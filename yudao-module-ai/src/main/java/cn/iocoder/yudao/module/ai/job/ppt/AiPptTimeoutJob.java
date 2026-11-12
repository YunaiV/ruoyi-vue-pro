package cn.iocoder.yudao.module.ai.job.ppt;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.ai.service.ppt.AiPptService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI PPT 超时 Job：生成中、且长时间没有进展的 PPT，置为已失败
 *
 * 例如服务重启中断了生成线程：否则一直是生成中，既不能继续生成，也不能删除；置为已失败后，用户可以继续生成
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class AiPptTimeoutJob implements JobHandler {

    @Resource
    private AiPptService pptService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = pptService.timeoutPpt();
        log.info("[execute][超时 AI PPT ({}) 个]", count);
        return String.format("超时 AI PPT %s 个", count);
    }

}
