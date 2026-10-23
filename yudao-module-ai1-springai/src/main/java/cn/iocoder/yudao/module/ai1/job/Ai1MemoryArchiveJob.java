package cn.iocoder.yudao.module.ai1.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.ai1.harness.memory.Ai1MemoryArchiveTool;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * AI1 长期记忆归档 Job
 *
 * @author 芋道源码
 */
@Component
public class Ai1MemoryArchiveJob implements JobHandler {

    @Resource
    private Ai1MemoryArchiveTool memoryArchiveTool;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = memoryArchiveTool.archive();
        return String.format("AI1 长期记忆归档：归档 %s 个用户", count);
    }

}
