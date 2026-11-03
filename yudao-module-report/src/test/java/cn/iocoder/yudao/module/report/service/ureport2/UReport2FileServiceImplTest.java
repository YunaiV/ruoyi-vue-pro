package cn.iocoder.yudao.module.report.service.ureport2;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.db.TenantDatabaseInterceptor;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.report.controller.admin.ureport2.vo.UReport2FilePageReqVO;
import cn.iocoder.yudao.module.report.controller.admin.ureport2.vo.UReport2FileSaveReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport2.UReport2FileDO;
import cn.iocoder.yudao.module.report.dal.mysql.ureport2.UReport2FileMapper;
import cn.iocoder.yudao.module.report.dal.redis.ureport2.UReport2FileVersionRedisDAO;
import cn.iocoder.yudao.module.report.framework.ureport2.core.cache.UReport2ReportDefinitionCache;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.bstek.ureport.definition.ReportDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.UREPORT2_FILE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UReport2FileServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({UReport2FileServiceImpl.class, UReport2ReportDefinitionCache.class,
        UReport2FileServiceImplTest.TenantTestConfiguration.class})
public class UReport2FileServiceImplTest extends BaseDbUnitTest {

    private static final Long TENANT_ID = 2L;
    private static final Long OTHER_TENANT_ID = 3L;
    private static final String NAME = "demo.ureport.xml";
    private static final String FILE = "db:" + NAME;
    private static final String VERSION = "test-version";

    /**
     * 多租户的测试配置：为 MyBatis Plus 增加租户插件，按租户隔离模板
     *
     * @author 芋道源码
     */
    @TestConfiguration(proxyBeanMethods = false)
    static class TenantTestConfiguration {

        @Bean
        static BeanPostProcessor tenantMybatisPlusInterceptorPostProcessor() {
            return new BeanPostProcessor() {

                @Override
                public Object postProcessBeforeInitialization(Object bean, String beanName) {
                    if (bean instanceof MybatisPlusInterceptor) {
                        TenantLineInnerInterceptor inner = new TenantLineInnerInterceptor(
                                new TenantDatabaseInterceptor(new TenantProperties()));
                        MyBatisUtils.addInterceptor((MybatisPlusInterceptor) bean, inner, 0);
                    }
                    return bean;
                }

            };
        }

    }

    @Resource
    private UReport2FileServiceImpl ureport2FileService;

    @Resource
    private UReport2FileMapper ureport2FileMapper;

    @MockBean
    private UReport2FileVersionRedisDAO ureport2FileVersionRedisDAO;

    @Resource
    private UReport2ReportDefinitionCache reportDefinitionCache;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.setTenantId(TENANT_ID);
        // mock 版本号
        when(ureport2FileVersionRedisDAO.refresh(anyLong(), anyString())).thenReturn(VERSION);
        clearReportDefinitionCache();
    }

    @AfterEach
    public void tearDown() {
        clearReportDefinitionCache();
        TenantContextHolder.clear();
    }

    /**
     * 清理测试用到的模板缓存：缓存是 Spring 容器中的单例，各测试之间共享，部分测试会保留其它租户的缓存
     */
    private void clearReportDefinitionCache() {
        TenantUtils.execute(TENANT_ID, () -> reportDefinitionCache.onFileDeleted(FILE));
        TenantUtils.execute(OTHER_TENANT_ID, () -> reportDefinitionCache.onFileDeleted(FILE));
        UReport2ReportDefinitionCache.clearPendingVersions();
    }

    @Test
    public void testSaveFile_create() {
        // 调用
        String version = ureport2FileService.saveFile(buildSaveReqVO(NAME, "<ureport>中文</ureport>"));
        // 断言
        assertEquals(VERSION, version);
        UReport2FileDO file = ureport2FileMapper.selectByName(NAME);
        assertNotNull(file);
        assertEquals("<ureport>中文</ureport>", file.getContent());
        assertEquals(TENANT_ID, file.getTenantId());
        // 断言：按当前租户、包含存储前缀的模板名称，发布版本号
        verify(ureport2FileVersionRedisDAO).refresh(eq(TENANT_ID), eq(FILE));
    }

    @Test
    public void testSaveFile_update() {
        // mock 数据
        ureport2FileService.saveFile(buildSaveReqVO(NAME, "<ureport>v1</ureport>"));
        Long id = ureport2FileMapper.selectByName(NAME).getId();

        // 调用
        ureport2FileService.saveFile(buildSaveReqVO(NAME, "<ureport>v2</ureport>"));
        // 断言：更新原记录，不新增
        assertEquals(1L, ureport2FileMapper.selectCount());
        UReport2FileDO file = ureport2FileMapper.selectById(id);
        assertEquals("<ureport>v2</ureport>", file.getContent());
    }

    @Test
    public void testSaveFile_trailingSpaceAlias() {
        // mock 数据：通过带尾部空格的别名保存
        ureport2FileService.saveFile(buildSaveReqVO(NAME + "  ", "<ureport>v1</ureport>"));

        // 调用：通过原名称保存
        ureport2FileService.saveFile(buildSaveReqVO(NAME, "<ureport>v2</ureport>"));
        // 断言：规范化为同一个名称，只有一条记录
        assertEquals(1L, ureport2FileMapper.selectCount());
        assertEquals(NAME, ureport2FileMapper.selectByName(NAME).getName());
        assertEquals("<ureport>v2</ureport>", ureport2FileService.getFileByName(NAME + " ").getContent());

        // 调用：通过别名删除
        ureport2FileService.deleteFileByName(NAME + " ");
        // 断言：原名称也查询不到，并且版本号都按规范化后的名称发布
        assertNull(ureport2FileService.getFileByName(NAME));
        verify(ureport2FileVersionRedisDAO, times(3)).refresh(eq(TENANT_ID), eq(FILE));
    }

    @Test
    public void testSaveFile_sameNameInOtherTenant() {
        // mock 数据：其它租户已有同名模板
        TenantUtils.execute(OTHER_TENANT_ID, () -> ureport2FileService.saveFile(
                buildSaveReqVO(NAME, "<ureport>other</ureport>")));

        // 调用：当前租户保存同名模板
        ureport2FileService.saveFile(buildSaveReqVO(NAME, "<ureport>mine</ureport>"));
        // 断言：各自独立，互不覆盖
        assertEquals("<ureport>mine</ureport>", ureport2FileService.getFileByName(NAME).getContent());
        assertEquals("<ureport>other</ureport>", TenantUtils.execute(OTHER_TENANT_ID,
                () -> ureport2FileService.getFileByName(NAME).getContent()));
        assertEquals(2L, TenantUtils.executeIgnore(() -> ureport2FileMapper.selectCount()));
    }

    @Test
    public void testGetFileList() {
        // mock 数据
        ureport2FileService.saveFile(buildSaveReqVO("b.ureport.xml", "<ureport>b</ureport>"));
        ureport2FileService.saveFile(buildSaveReqVO("a.ureport.xml", "<ureport>a</ureport>"));
        TenantUtils.execute(OTHER_TENANT_ID, () -> ureport2FileService.saveFile(
                buildSaveReqVO("c.ureport.xml", "<ureport>c</ureport>")));

        // 调用
        List<UReport2FileDO> list = ureport2FileService.getFileList();
        // 断言：只返回当前租户，按名称排序，不查询模板内容
        assertEquals(2, list.size());
        assertEquals("a.ureport.xml", list.get(0).getName());
        assertEquals("b.ureport.xml", list.get(1).getName());
        assertNull(list.get(0).getContent());
        assertNotNull(list.get(0).getUpdateTime());
    }

    @Test
    public void testGetFilePage() {
        // mock 数据：当前租户
        ureport2FileService.saveFile(buildSaveReqVO("demo-a.ureport.xml", "<ureport>a</ureport>"));
        ureport2FileService.saveFile(buildSaveReqVO("other.ureport.xml", "<ureport>other</ureport>"));
        ureport2FileService.saveFile(buildSaveReqVO("demo-b.ureport.xml", "<ureport>b</ureport>"));
        // mock 数据：已删除的模板、其它租户的模板，都不返回
        ureport2FileService.saveFile(buildSaveReqVO("demo-deleted.ureport.xml", "<ureport>deleted</ureport>"));
        ureport2FileService.deleteFileByName("demo-deleted.ureport.xml");
        TenantUtils.execute(OTHER_TENANT_ID, () -> ureport2FileService.saveFile(
                buildSaveReqVO("demo-c.ureport.xml", "<ureport>c</ureport>")));
        // 准备参数
        UReport2FilePageReqVO reqVO = new UReport2FilePageReqVO();
        reqVO.setName("demo");
        reqVO.setPageNo(1);
        reqVO.setPageSize(1);

        // 调用：第一页
        PageResult<UReport2FileDO> pageResult = ureport2FileService.getFilePage(reqVO);
        // 断言：按名称模糊匹配，只返回当前租户未删除的模板；按编号倒序，不查询模板内容
        assertEquals(2L, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        UReport2FileDO file = pageResult.getList().get(0);
        assertEquals("demo-b.ureport.xml", file.getName());
        assertNull(file.getContent());
        assertNotNull(file.getCreateTime());
        assertNotNull(file.getUpdateTime());

        // 调用：第二页
        reqVO.setPageNo(2);
        pageResult = ureport2FileService.getFilePage(reqVO);
        // 断言
        assertEquals(2L, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertEquals("demo-a.ureport.xml", pageResult.getList().get(0).getName());
    }

    @Test
    public void testDeleteFileByName_success() {
        // mock 数据
        ureport2FileService.saveFile(buildSaveReqVO(NAME, "<ureport>v1</ureport>"));
        Long oldId = ureport2FileMapper.selectByName(NAME).getId();

        // 调用
        ureport2FileService.deleteFileByName(NAME);
        // 断言：逻辑删除后，查询不到
        assertNull(ureport2FileService.getFileByName(NAME));
        assertNull(ureport2FileMapper.selectById(oldId));
        // 断言：保存、删除各发布一次版本号
        verify(ureport2FileVersionRedisDAO, times(2)).refresh(eq(TENANT_ID), eq(FILE));
        // 断言：可以重新创建同名模板，并且是新的记录
        ureport2FileService.saveFile(buildSaveReqVO(NAME, "<ureport>v2</ureport>"));
        UReport2FileDO file = ureport2FileService.getFileByName(NAME);
        assertEquals("<ureport>v2</ureport>", file.getContent());
        assertNotEquals(oldId, file.getId());
    }

    @Test
    public void testDeleteFile_success() {
        // mock 数据
        ureport2FileService.saveFile(buildSaveReqVO(NAME, "<ureport>v1</ureport>"));
        Long id = ureport2FileMapper.selectByName(NAME).getId();

        // 调用
        ureport2FileService.deleteFile(id);
        // 断言：逻辑删除后，查询不到；保存、删除各发布一次版本号
        assertNull(ureport2FileService.getFileByName(NAME));
        assertNull(ureport2FileMapper.selectById(id));
        verify(ureport2FileVersionRedisDAO, times(2)).refresh(eq(TENANT_ID), eq(FILE));
    }

    @Test
    public void testDeleteFile_legacyTrailingSpaces() {
        // mock 数据：规范化之前保存的旧数据，名称带有尾部空格
        UReport2FileDO legacyFile = new UReport2FileDO().setName(NAME + "  ").setContent("<ureport>legacy</ureport>");
        ureport2FileMapper.insert(legacyFile);

        // 调用
        ureport2FileService.deleteFile(legacyFile.getId());
        // 断言：按规范化后的名称发布版本号，与模板缓存使用的名称一致
        assertNull(ureport2FileMapper.selectById(legacyFile.getId()));
        verify(ureport2FileVersionRedisDAO).refresh(eq(TENANT_ID), eq(FILE));
    }

    @Test
    public void testDeleteFile_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> ureport2FileService.deleteFile(1024L), UREPORT2_FILE_NOT_EXISTS);
        // 断言：没有删除，不发布版本号
        verify(ureport2FileVersionRedisDAO, never()).refresh(anyLong(), anyString());
    }

    @Test
    public void testDeleteFile_otherTenant() {
        // mock 数据：只有其它租户存在该模板
        Long id = TenantUtils.execute(OTHER_TENANT_ID, () -> {
            ureport2FileService.saveFile(buildSaveReqVO(NAME, "<ureport>other</ureport>"));
            return ureport2FileMapper.selectByName(NAME).getId();
        });

        // 调用，并断言异常：当前租户无法按编号删除其它租户的模板
        assertServiceException(() -> ureport2FileService.deleteFile(id), UREPORT2_FILE_NOT_EXISTS);
        assertNotNull(TenantUtils.execute(OTHER_TENANT_ID, () -> ureport2FileMapper.selectById(id)));
        verify(ureport2FileVersionRedisDAO, never()).refresh(eq(TENANT_ID), anyString());
    }

    @Test
    public void testDeleteFileByName_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> ureport2FileService.deleteFileByName(NAME), UREPORT2_FILE_NOT_EXISTS);
        // 断言：没有删除，不发布版本号
        verify(ureport2FileVersionRedisDAO, never()).refresh(anyLong(), anyString());
    }

    @Test
    public void testDeleteFileByName_otherTenant() {
        // mock 数据：只有其它租户存在该模板
        TenantUtils.execute(OTHER_TENANT_ID, () -> ureport2FileService.saveFile(
                buildSaveReqVO(NAME, "<ureport>other</ureport>")));

        // 调用，并断言异常：当前租户无法删除其它租户的模板
        assertServiceException(() -> ureport2FileService.deleteFileByName(NAME), UREPORT2_FILE_NOT_EXISTS);
        assertNotNull(TenantUtils.execute(OTHER_TENANT_ID, () -> ureport2FileService.getFileByName(NAME)));
    }

    @Test
    public void testDeleteFile_evictReportDefinitionCache() {
        // mock 数据：当前租户、其它租户的同名模板，都已缓存
        ureport2FileService.saveFile(buildSaveReqVO(NAME, "<ureport>v1</ureport>"));
        Long id = ureport2FileMapper.selectByName(NAME).getId();
        cacheReportDefinition();
        ReportDefinition otherDefinition = TenantUtils.execute(OTHER_TENANT_ID, () -> cacheReportDefinition());

        // 调用：管理后台按编号删除
        ureport2FileService.deleteFile(id);
        // 断言：移除本实例当前租户的缓存，其它租户的缓存不受影响
        assertReportDefinitionEvicted(otherDefinition);
    }

    @Test
    public void testDeleteFileByName_evictReportDefinitionCache() {
        // mock 数据：当前租户、其它租户的同名模板，都已缓存
        ureport2FileService.saveFile(buildSaveReqVO(NAME, "<ureport>v1</ureport>"));
        cacheReportDefinition();
        ReportDefinition otherDefinition = TenantUtils.execute(OTHER_TENANT_ID, () -> cacheReportDefinition());

        // 调用：设计器按名称删除（UReport2DatabaseReportProvider#deleteReport）
        ureport2FileService.deleteFileByName(NAME);
        // 断言：移除本实例当前租户的缓存，其它租户的缓存不受影响
        assertReportDefinitionEvicted(otherDefinition);
    }

    @Test
    public void testDeleteFile_evictReportDefinitionCacheWhenPublishFail() {
        // mock 数据：当前租户、其它租户的同名模板，都已缓存
        ureport2FileService.saveFile(buildSaveReqVO(NAME, "<ureport>v1</ureport>"));
        Long id = ureport2FileMapper.selectByName(NAME).getId();
        cacheReportDefinition();
        ReportDefinition otherDefinition = TenantUtils.execute(OTHER_TENANT_ID, () -> cacheReportDefinition());
        // mock 方法：发布版本号失败，例如说 Redis 不可用
        when(ureport2FileVersionRedisDAO.refresh(eq(TENANT_ID), eq(FILE))).thenThrow(new IllegalStateException("Redis 不可用"));

        // 调用，并断言异常
        assertThrows(IllegalStateException.class, () -> ureport2FileService.deleteFile(id));
        // 断言：数据库已删除，本实例也不再保留已删除模板的缓存
        assertNull(ureport2FileMapper.selectById(id));
        assertReportDefinitionEvicted(otherDefinition);
    }

    /**
     * 按 UReport2 读取模板的流程（未命中、加载并解析、写入缓存），缓存当前租户的模板定义
     *
     * @return 缓存的模板定义
     */
    private ReportDefinition cacheReportDefinition() {
        // mock 版本号：删除前后保持不变，用于验证缓存对象本身被移除，而不只是版本号失效
        when(ureport2FileVersionRedisDAO.get(anyLong(), eq(FILE))).thenReturn(VERSION);
        ReportDefinition definition = new ReportDefinition();
        assertNull(reportDefinitionCache.getReportDefinition(FILE));
        reportDefinitionCache.cacheReportDefinition(FILE, definition);
        assertSame(definition, reportDefinitionCache.getReportDefinition(FILE));
        return definition;
    }

    private void assertReportDefinitionEvicted(ReportDefinition otherDefinition) {
        // 当前租户：版本号不变，也读取不到缓存
        assertNull(reportDefinitionCache.getReportDefinition(FILE));
        // 其它租户：仍然命中原缓存
        assertSame(otherDefinition, TenantUtils.execute(OTHER_TENANT_ID, () -> reportDefinitionCache.getReportDefinition(FILE)));
        // 本地缓存只剩其它租户的模板定义，已删除模板的对象已释放
        Map<?, ?> reportDefinitions = (Map<?, ?>) ReflectionTestUtils.getField(reportDefinitionCache, "reportDefinitions");
        assertNotNull(reportDefinitions);
        assertEquals(1, reportDefinitions.size());
    }

    private static UReport2FileSaveReqVO buildSaveReqVO(String name, String content) {
        return new UReport2FileSaveReqVO().setName(name).setContent(content);
    }

}
