package cn.iocoder.yudao.module.pms.job.kb;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.pms.service.kb.recycle.PmsKnowledgeRecycleService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * PMS 知识库回收站到期清理 Job
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class PmsKnowledgeRecycleCleanJob implements JobHandler {

    private static final int RETAIN_DAYS = 30;

    @Resource
    private PmsKnowledgeRecycleService recycleService;

    @Override
    @TenantJob
    public String execute(String param) {
        LocalDateTime deleteTime = LocalDateTime.now().minusDays(RETAIN_DAYS);
        int cleanCount = recycleService.deleteExpiredRecycleRecords(deleteTime);
        log.info("[execute][清理知识库到期回收站记录 ({}) 条]", cleanCount);
        return String.format("清理知识库到期回收站记录 %s 条", cleanCount);
    }

}
