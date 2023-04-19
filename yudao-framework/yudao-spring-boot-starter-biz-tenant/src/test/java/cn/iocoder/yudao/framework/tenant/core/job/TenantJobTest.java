package cn.iocoder.yudao.framework.tenant.core.job;

import cn.iocoder.yudao.framework.tenant.core.service.TenantFrameworkService;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 验证 job 租户逻辑
 * {@link TenantJobHandlerDecorator}
 *
 * @author gaibu
 */
public class TenantJobTest extends BaseMockitoUnitTest {

    @Mock
    TenantFrameworkService tenantFrameworkService;

    @Test
    public void test() throws Exception {
        // 准备测试租户 id
        List<Long> tenantIds = Lists.newArrayList(1L, 2L, 3L);
        // mock 数据
        Mockito.doReturn(tenantIds).when(tenantFrameworkService).getTenantIds();
        // 准备测试任务
        TestJob testJob = new TestJob();
        // 创建任务装饰器
        TenantJobHandlerDecorator tenantJobHandlerDecorator = new TenantJobHandlerDecorator(tenantFrameworkService, testJob);

        // 执行任务
        tenantJobHandlerDecorator.execute(null);

        // 断言返回值
        assertEquals(testJob.getTenantIds(), tenantIds);
    }
}
