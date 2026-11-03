package cn.iocoder.yudao.module.report.framework.ureport2.core.web;

import cn.iocoder.yudao.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import cn.iocoder.yudao.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.service.TenantFrameworkService;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.web.core.handler.GlobalExceptionHandler;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.report.dal.redis.ureport2.UReport2FileVersionRedisDAO;
import cn.iocoder.yudao.module.report.framework.ureport2.core.cache.UReport2ReportDefinitionCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StreamUtils;

import javax.servlet.FilterChain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.module.report.enums.PermissionConstants.UREPORT2_DESIGN;
import static cn.iocoder.yudao.module.report.enums.PermissionConstants.UREPORT2_QUERY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UReport2SecurityFilter} 的单元测试类，校验查看、设计权限可访问的请求
 *
 * @author 芋道源码
 */
public class UReport2SecurityFilterTest extends BaseMockitoUnitTest {

    /**
     * 已保存的报表：前端 _u=db: + 两次 encodeURIComponent 的名称，Servlet 解码一次后的值
     */
    private static final String SAVED_FILE = "db:%E9%94%80%E5%94%AE%E6%8A%A5%E8%A1%A8";
    /**
     * 请求之前的租户编号，与登录用户的租户不同
     */
    private static final Long OLD_TENANT_ID = 99L;
    /**
     * 异常日志不能包含的敏感数据：token、模板内容中的数据源密码、JSON 请求体
     */
    private static final String FAKE_TOKEN = "fake-refresh-token-7f3a9c";
    private static final String FAKE_PASSWORD = "FakeDatasourcePassword42";
    private static final String FAKE_CONTENT = "<ureport><datasource name=\"demo\" type=\"jdbc\" username=\"root\" password=\""
            + FAKE_PASSWORD + "\"/></ureport>";
    private static final String FAKE_BODY_PASSWORD = "FakeJsonBodyPassword42";
    private static final String FAKE_BODY = "{\"password\":\"" + FAKE_BODY_PASSWORD + "\"}";

    private UReport2SecurityFilter filter;

    @Mock
    private SecurityFrameworkService securityFrameworkService;
    @Mock
    private GlobalExceptionHandler globalExceptionHandler;
    @Mock
    private TenantFrameworkService tenantFrameworkService;
    @Mock
    private UReport2FileVersionRedisDAO fileVersionRedisDAO;
    @Mock
    private ApiErrorLogCommonApi apiErrorLogApi;

    @BeforeEach
    public void setUp() {
        // 关闭多租户，只校验权限
        filter = new UReport2SecurityFilter(securityFrameworkService, null, globalExceptionHandler);
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(1L).setTenantId(1L)
                .setUserType(UserTypeEnum.ADMIN.getValue()), new MockHttpServletRequest());
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
        UReport2ReportDefinitionCache.clearPendingVersions();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // 预览、分页、打印
            "/ureport/preview", "/ureport/preview/", "/ureport/preview/loadData",
            "/ureport/preview/loadPrintPages", "/ureport/preview/loadPagePaper",
            // 导出 Excel
            "/ureport/excel", "/ureport/excel/paging", "/ureport/excel/sheet",
            "/ureport/excel97", "/ureport/excel97/paging", "/ureport/excel97/sheet",
            // 导出 PDF、PDF 在线打印
            "/ureport/pdf", "/ureport/pdf/show", "/ureport/pdf/newPaging",
            // 导出 Word
            "/ureport/word",
            // 回传图表图片
            "/ureport/chart/storeData"
    })
    public void testDoFilter_query_allowed(String uri) throws Exception {
        // mock 方法：只有查看权限
        mockPermissions(false, true);
        MockHttpServletRequest request = buildRequest(uri, SAVED_FILE);

        // 调用，并断言
        assertAllowed(request);
    }

    @Test
    public void testDoFilter_query_allowedWithContextPath() throws Exception {
        // mock 方法：只有查看权限
        mockPermissions(false, true);
        MockHttpServletRequest request = buildRequest("/admin/ureport/preview", SAVED_FILE);
        request.setContextPath("/admin");

        // 调用，并断言
        assertAllowed(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // 设计器：页面、保存、删除、加载、脚本校验、设计中报表的预览
            "/ureport/designer", "/ureport/designer/saveReportFile", "/ureport/designer/deleteReportFile",
            "/ureport/designer/loadReport", "/ureport/designer/loadReportProviders",
            "/ureport/designer/scriptValidation", "/ureport/designer/conditionScriptValidation",
            "/ureport/designer/parseDatasetName", "/ureport/designer/savePreviewData",
            // 数据源：测试连接、执行 SQL、生成类
            "/ureport/datasource/testConnection", "/ureport/datasource/previewData",
            "/ureport/datasource/buildDatabaseTables", "/ureport/datasource/buildFields",
            "/ureport/datasource/buildClass", "/ureport/datasource/loadBuildinDatasources",
            "/ureport/datasource/loadMethods",
            // 其它的设计功能
            "/ureport/import", "/ureport/searchFormDesigner", "/ureport/image", "/ureport/res/designer.html",
            // 白名单 Action 的其它方法，例如说 execute 会被反射调用
            "/ureport/preview/execute", "/ureport/chart", "/ureport/excel/buildExcel", "/ureport/pdf/buildPdf",
            "/ureport/word/buildWord",
            // 不存在的 Action
            "/ureport", "/ureport/", "/ureport/unknown", "/ureport/unknown/loadData",
            // 大小写、编码、路径参数、多余的斜杠：与 UReportServlet 的分发结果不一致，不能放行
            "/ureport/Preview", "/ureport/PREVIEW/loadData", "/ureport/preview/LoadData",
            "/ureport/%70review", "/ureport/preview%2FloadData", "/ureport/preview/load%44ata",
            "/ureport/preview;jsessionid=1", "/ureport/preview/loadData;a=b",
            "/ureport/preview/loadData/", "/ureport//preview", "/ureport/preview/../designer",
            "/ureport/pdf/newPaging/x", "/ureport/excel97x"
    })
    public void testDoFilter_query_forbiddenAction(String uri) throws Exception {
        // mock 方法：只有查看权限
        mockPermissions(false, true);
        MockHttpServletRequest request = buildRequest(uri, SAVED_FILE);

        // 调用，并断言
        assertForbidden(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // 空
            "",
            // 设计器中未保存的报表：直接传递、编码后传递
            "p", "%70", "%2570",
            // 其它的报表来源、大小写不一致、编码后的前缀
            "file:demo.ureport.xml", "classpath:demo.ureport.xml", "DB:demo", "Db:demo",
            "%64b:demo", "db%3Ademo", " db:demo"
    })
    public void testDoFilter_query_forbiddenFile(String file) throws Exception {
        // mock 方法：只有查看权限
        mockPermissions(false, true);
        MockHttpServletRequest request = buildRequest("/ureport/preview", file);

        // 调用，并断言
        assertForbidden(request);
    }

    @Test
    public void testDoFilter_query_forbiddenWithoutFile() throws Exception {
        // mock 方法：只有查看权限
        mockPermissions(false, true);
        MockHttpServletRequest request = buildRequest("/ureport/pdf/newPaging", null);

        // 调用，并断言
        assertForbidden(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/ureport/designer", "/ureport/designer/saveReportFile", "/ureport/designer/savePreviewData",
            "/ureport/datasource/testConnection", "/ureport/import", "/ureport/preview"
    })
    public void testDoFilter_design_allowed(String uri) throws Exception {
        // mock 方法：只有设计权限
        mockPermissions(true, false);
        // 设计权限可访问设计中的报表
        MockHttpServletRequest request = buildRequest(uri, "p");

        // 调用，并断言
        assertAllowed(request);
    }

    @Test
    public void testDoFilter_noPermission() throws Exception {
        // mock 方法：没有任何权限
        mockPermissions(false, false);
        MockHttpServletRequest request = buildRequest("/ureport/preview", SAVED_FILE);

        // 调用，并断言
        assertForbidden(request);
    }

    @Test
    public void testDoFilter_permissionException() throws Exception {
        // 准备参数：开启多租户，并设置请求之前的租户上下文
        filter = new UReport2SecurityFilter(securityFrameworkService, tenantFrameworkService, globalExceptionHandler);
        TenantContextHolder.setTenantId(OLD_TENANT_ID);
        TenantContextHolder.setIgnore(true);
        MockHttpServletRequest request = buildRequest("/ureport/preview", SAVED_FILE);
        // mock 方法：权限校验异常，例如说查询用户权限的 RPC 调用失败；异常之前，已在登录用户的租户下读取模板缓存，记录了待写入的版本号
        IllegalStateException exception = new IllegalStateException("权限服务不可用");
        when(securityFrameworkService.hasPermission(UREPORT2_DESIGN)).thenAnswer(invocation -> {
            assertEquals(1L, TenantContextHolder.getTenantId());
            assertNull(new UReport2ReportDefinitionCache(fileVersionRedisDAO).getReportDefinition("db:demo.ureport.xml"));
            assertNotNull(getPendingVersions().get());
            throw exception;
        });
        when(globalExceptionHandler.allExceptionHandler(any(), same(exception)))
                .thenReturn(CommonResult.error(INTERNAL_SERVER_ERROR));

        // 调用
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        // 断言：不放行，返回 GlobalExceptionHandler 处理的结果
        assertNull(chain.getRequest());
        CommonResult<?> result = JsonUtils.parseObject(response.getContentAsString(), CommonResult.class);
        assertNotNull(result);
        assertEquals(INTERNAL_SERVER_ERROR.getCode(), result.getCode());
        // 断言：清理待写入的版本号，恢复请求之前的租户上下文
        assertNull(getPendingVersions().get());
        assertEquals(OLD_TENANT_ID, TenantContextHolder.getTenantId());
        assertTrue(TenantContextHolder.isIgnore());
    }

    @Test
    public void testDoFilter_chainException() throws Exception {
        // 准备参数：开启多租户，并设置请求之前的租户上下文
        filter = new UReport2SecurityFilter(securityFrameworkService, tenantFrameworkService, globalExceptionHandler);
        TenantContextHolder.setTenantId(OLD_TENANT_ID);
        TenantContextHolder.setIgnore(true);
        MockHttpServletRequest request = buildRequest("/ureport/preview", SAVED_FILE);
        // mock 方法：只有查看权限；后续的过滤链异常，例如说导出时写入响应失败
        mockPermissions(false, true);
        FilterChain chain = mock(FilterChain.class);
        IOException exception = new IOException("导出失败");
        doThrow(exception).when(chain).doFilter(any(), any());

        // 调用，并断言：过滤链的异常原样抛出，不经过权限校验的异常处理
        assertSame(exception, assertThrows(IOException.class,
                () -> filter.doFilter(request, new MockHttpServletResponse(), chain)));
        verify(globalExceptionHandler, never()).allExceptionHandler(any(), any());
        // 断言：恢复请求之前的租户上下文
        assertEquals(OLD_TENANT_ID, TenantContextHolder.getTenantId());
        assertTrue(TenantContextHolder.isIgnore());
    }

    @Test
    public void testDoFilter_permissionException_errorLogWithoutSensitiveData() throws Exception {
        // 准备参数：使用真实的 GlobalExceptionHandler，记录异常日志
        filter = new UReport2SecurityFilter(securityFrameworkService, null,
                new GlobalExceptionHandler("test", apiErrorLogApi));
        MockHttpServletRequest request = buildSensitiveRequest();
        // mock 方法：权限校验异常
        when(securityFrameworkService.hasPermission(UREPORT2_DESIGN)).thenThrow(new IllegalStateException("权限服务不可用"));

        // 调用，并断言
        assertErrorLogWithoutSensitiveData(request);
    }

    @Test
    public void testDoFilter_tenantException_errorLogWithoutSensitiveData() throws Exception {
        // 准备参数：开启多租户，使用真实的 GlobalExceptionHandler，记录异常日志
        filter = new UReport2SecurityFilter(securityFrameworkService, tenantFrameworkService,
                new GlobalExceptionHandler("test", apiErrorLogApi));
        MockHttpServletRequest request = buildSensitiveRequest();
        // mock 方法：租户校验异常
        doThrow(new IllegalStateException("租户服务不可用")).when(tenantFrameworkService).validTenant(eq(1L));

        // 调用，并断言
        assertErrorLogWithoutSensitiveData(request);
    }

    /**
     * 构建包含敏感数据的请求：设计器保存模板，参数包含 token、模板内容，并带有 JSON 请求体
     */
    private static MockHttpServletRequest buildSensitiveRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/ureport/designer/saveReportFile");
        request.addParameter("token", FAKE_TOKEN);
        request.addParameter("tenantId", "1");
        request.addParameter("_u", SAVED_FILE);
        request.addParameter("content", FAKE_CONTENT);
        request.setContentType("application/json");
        request.setContent(FAKE_BODY.getBytes(StandardCharsets.UTF_8));
        // 登录用户：与 TokenAuthenticationFilter 一致，记录在请求属性中；GlobalExceptionHandler 记录日志时读取
        WebFrameworkUtils.setLoginUserId(request, 1L);
        WebFrameworkUtils.setLoginUserType(request, UserTypeEnum.ADMIN.getValue());
        return request;
    }

    private void assertErrorLogWithoutSensitiveData(MockHttpServletRequest request) throws Exception {
        // 调用
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        // 断言：不放行，返回系统异常
        assertNull(chain.getRequest());
        CommonResult<?> result = JsonUtils.parseObject(response.getContentAsString(), CommonResult.class);
        assertNotNull(result);
        assertEquals(INTERNAL_SERVER_ERROR.getCode(), result.getCode());

        // 断言：记录了异常日志，保留 URL、请求方法、登录用户等定位信息
        ArgumentCaptor<ApiErrorLogCreateReqDTO> errorLogCaptor = ArgumentCaptor.forClass(ApiErrorLogCreateReqDTO.class);
        verify(apiErrorLogApi).createApiErrorLogAsync(errorLogCaptor.capture());
        ApiErrorLogCreateReqDTO errorLog = errorLogCaptor.getValue();
        assertEquals("/ureport/designer/saveReportFile", errorLog.getRequestUrl());
        assertEquals("POST", errorLog.getRequestMethod());
        assertEquals(1L, errorLog.getUserId());
        assertEquals(UserTypeEnum.ADMIN.getValue(), errorLog.getUserType());
        // 断言：异常日志不包含请求参数、请求体中的敏感数据
        String errorLogJson = JsonUtils.toJsonString(errorLog);
        assertFalse(errorLogJson.contains(FAKE_TOKEN), errorLogJson);
        assertFalse(errorLogJson.contains(FAKE_PASSWORD), errorLogJson);
        assertFalse(errorLogJson.contains(FAKE_BODY_PASSWORD), errorLogJson);

        // 断言：原请求未改变，参数、请求体仍可读取
        assertEquals(FAKE_TOKEN, request.getParameter("token"));
        assertEquals(FAKE_CONTENT, request.getParameter("content"));
        assertEquals(FAKE_BODY, StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8));
    }

    private void mockPermissions(boolean design, boolean query) {
        lenient().when(securityFrameworkService.hasPermission(UREPORT2_DESIGN)).thenReturn(design);
        lenient().when(securityFrameworkService.hasPermission(UREPORT2_QUERY)).thenReturn(query);
    }

    private static MockHttpServletRequest buildRequest(String uri, String file) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        if (file != null) {
            request.addParameter("_u", file);
        }
        return request;
    }

    private void assertAllowed(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertInstanceOf(UReport2SessionRequestWrapper.class, chain.getRequest(),
                () -> "应放行：" + request.getRequestURI());
        assertEquals("", response.getContentAsString());
    }

    private static ThreadLocal<?> getPendingVersions() {
        return (ThreadLocal<?>) ReflectionTestUtils.getField(UReport2ReportDefinitionCache.class, "PENDING_VERSIONS");
    }

    private void assertForbidden(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertNull(chain.getRequest(), () -> "应拒绝：" + request.getRequestURI());
        CommonResult<?> result = JsonUtils.parseObject(response.getContentAsString(), CommonResult.class);
        assertNotNull(result);
        assertEquals(FORBIDDEN.getCode(), result.getCode());
    }

}
