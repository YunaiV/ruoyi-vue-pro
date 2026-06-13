package cn.iocoder.yudao.module.system.job.token;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 物理删除过期 N 天的令牌的 Job
 *
 * @author preschooler
 */
@Component
@Slf4j
public class TokenCleanJob implements JobHandler {

    @Resource
    private OAuth2TokenService oauth2TokenService;

    /**
     * 清理过期（14）天的令牌
     */
    private static final Integer JOB_CLEAN_RETAIN_DAY = 14;

    /**
     * 每次删除间隔的条数，如果值太高可能会造成数据库的压力过大
     */
    private static final Integer DELETE_LIMIT = 100;

    @Override
    @TenantIgnore
    public String execute(String param) {
        Integer refreshCount = oauth2TokenService.cleanRefreshToken(JOB_CLEAN_RETAIN_DAY, DELETE_LIMIT);
        log.info("[execute][定时执行清理刷新令牌数量 ({}) 个]", refreshCount);
        Integer accessCount = oauth2TokenService.cleanAccessToken(JOB_CLEAN_RETAIN_DAY, DELETE_LIMIT);
        log.info("[execute][定时执行清理访问令牌数量 ({}) 个]", accessCount);
        return String.format("定时执行清理刷新令牌数量 %s 个，访问令牌数量 %s 个", refreshCount, accessCount);
    }

}
