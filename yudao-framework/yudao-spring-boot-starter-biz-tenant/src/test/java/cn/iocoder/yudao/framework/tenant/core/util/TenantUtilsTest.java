package cn.iocoder.yudao.framework.tenant.core.util;

import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class TenantUtilsTest {

    @Test
    void shouldReadTenantEnableOnlyOnce() {
        // 准备多租户开关
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            springUtil.when(() -> SpringUtil.getProperty("yudao.tenant.enable", Boolean.class, true))
                    .thenReturn(true);

            // 多次访问只读取一次配置
            assertTrue(TenantUtils.isTenantEnable());
            assertTrue(TenantUtils.isTenantEnable());
            springUtil.verify(() -> SpringUtil.getProperty("yudao.tenant.enable", Boolean.class, true), times(1));
        }
    }

}
