package cn.iocoder.yudao.module.report.service.ureport2;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.report.controller.admin.ureport2.vo.UReport2FileSaveReqVO;
import cn.iocoder.yudao.module.report.dal.mysql.ureport2.UReport2FileMapper;
import cn.iocoder.yudao.module.report.dal.redis.ureport2.UReport2FileVersionRedisDAO;
import cn.iocoder.yudao.module.report.framework.ureport2.core.cache.UReport2ReportDefinitionCache;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * {@link UReport2FileServiceImpl} 在关闭 UReport2（ureport.enabled=false）时的单元测试类
 *
 * 此时 UReport2Configuration 不生效，没有 {@link UReport2ReportDefinitionCache} Bean，Service 仍然可以创建，并正常删除模板
 *
 * @author 芋道源码
 */
@Import({UReport2FileServiceImpl.class, UReport2FileServiceImplTest.TenantTestConfiguration.class})
public class UReport2FileServiceImplWithoutCacheTest extends BaseDbUnitTest {

    private static final Long TENANT_ID = 2L;
    private static final String NAME = "demo.ureport.xml";

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private UReport2FileServiceImpl ureport2FileService;

    @Resource
    private UReport2FileMapper ureport2FileMapper;

    @MockitoBean
    private UReport2FileVersionRedisDAO ureport2FileVersionRedisDAO;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.setTenantId(TENANT_ID);
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testDeleteFile_withoutReportDefinitionCache() {
        // 断言：没有模板缓存 Bean
        assertNull(applicationContext.getBeanProvider(UReport2ReportDefinitionCache.class).getIfAvailable());
        // mock 数据
        ureport2FileService.saveFile(new UReport2FileSaveReqVO().setName(NAME).setContent("<ureport>v1</ureport>"));
        Long id = ureport2FileMapper.selectByName(NAME).getId();
        assertNotNull(id);

        // 调用
        ureport2FileService.deleteFile(id);
        // 断言：正常删除，保存、删除各发布一次版本号
        assertNull(ureport2FileMapper.selectById(id));
        verify(ureport2FileVersionRedisDAO, times(2)).refresh(eq(TENANT_ID), eq("db:" + NAME));
    }

}
