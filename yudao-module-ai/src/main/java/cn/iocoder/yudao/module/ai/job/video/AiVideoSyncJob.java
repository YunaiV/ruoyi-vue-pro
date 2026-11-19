package cn.iocoder.yudao.module.ai.job.video;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.ai.service.video.AiVideoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI 视频同步 Job：查询服务商生成进度、转存文件、处理超时
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class AiVideoSyncJob implements JobHandler {

    @Resource
    private AiVideoService videoService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = videoService.syncVideo();
        log.info("[execute][同步 AI 视频 ({}) 个]", count);
        return String.format("同步 AI 视频 %s 个", count);
    }

}
