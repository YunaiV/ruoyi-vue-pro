package cn.iocoder.yudao.module.ai.job.drama;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaStoryboardService;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaTaskService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI 短剧同步 Job：查询镜头视频的生成进度并转存；处理出图、镜头视频、成片的超时
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class AiDramaSyncJob implements JobHandler {

    @Resource
    private AiDramaStoryboardService storyboardService;
    @Resource
    private AiDramaTaskService taskService;

    @Override
    @TenantJob
    public String execute(String param) {
        int syncCount = storyboardService.syncStoryboardVideo();
        int timeoutCount = taskService.timeoutTasks();
        log.info("[execute][同步镜头视频 ({}) 个，超时 ({}) 个]", syncCount, timeoutCount);
        return String.format("同步镜头视频 %s 个，超时 %s 个", syncCount, timeoutCount);
    }

}
