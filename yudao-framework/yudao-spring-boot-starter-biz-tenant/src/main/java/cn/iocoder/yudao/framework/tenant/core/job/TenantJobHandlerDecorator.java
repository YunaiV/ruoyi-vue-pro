package cn.iocoder.yudao.framework.tenant.core.job;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.service.TenantFrameworkService;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多租户 JobHandler 装饰器
 * 任务执行时，会按照租户逐个执行 Job 的逻辑
 *
 * 注意，需要保证 JobHandler 的幂等性。因为 Job 因为某个租户执行失败重试时，之前执行成功的租户也会再次执行。
 *
 * @author 芋道源码
 */
@AllArgsConstructor
public class TenantJobHandlerDecorator implements JobHandler {

    private final TenantFrameworkService tenantFrameworkService;
    /**
     * 被装饰的 Job
     */
    private final JobHandler jobHandler;

    @Override
    public final String execute(String param) throws Exception {
        // 获得租户列表
        List<Long> tenantIds = tenantFrameworkService.getTenantIds();
        if (CollUtil.isEmpty(tenantIds)) {
            return null;
        }

        // 逐个租户，执行 Job
        Map<Long, String> results = new ConcurrentHashMap<>();
        tenantIds.parallelStream().forEach(tenantId -> { // TODO 芋艿：先通过 parallel 实现并行；1）多个租户，是一条执行日志；2）异常的情况
            try {
                // 设置租户
                TenantContextHolder.setTenantId(tenantId);
                // 执行 Job
                String result = jobHandler.execute(param);
                // 添加结果
                results.put(tenantId, result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                TenantContextHolder.clear();
            }
        });
        return JsonUtils.toJsonString(results);
    }

}
