package cn.iocoder.yudao.module.report.framework.ureport2.core.web;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.service.TenantFrameworkService;
import cn.iocoder.yudao.framework.web.core.handler.GlobalExceptionHandler;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.report.framework.ureport2.core.cache.UReport2ReportDefinitionCache;
import cn.iocoder.yudao.module.report.framework.ureport2.core.provider.UReport2DatabaseReportProvider;
import com.bstek.ureport.console.UReportServlet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.module.report.enums.PermissionConstants.UREPORT2_DESIGN;
import static cn.iocoder.yudao.module.report.enums.PermissionConstants.UREPORT2_QUERY;

/**
 * UReport2 请求的安全过滤器
 *
 * UReport2 的请求（/ureport/**）不属于 /admin-api/**，TenantSecurityWebFilter 不会处理它，所以由本过滤器补充：
 * 1. 校验登录用户是管理后台用户，并且拥有 UReport2 权限：设计权限可访问全部请求；查看权限只能访问 {@link #QUERY_ACTIONS}，并且只能访问已保存的报表
 * 2. 基于已认证的 {@link LoginUser} 设置租户上下文；如果传递了 tenant-id Header 或 tenantId 参数，则必须与登录用户的租户一致
 * 3. 校验租户是合法，例如说被禁用、到期
 * 4. 通过 {@link UReport2SessionRequestWrapper} 为 UReport2 的 Session 缓存提供稳定编号
 * 5. 请求结束时，清理 {@link UReport2ReportDefinitionCache} 记录在当前线程的待写入版本号，避免线程池复用时累积
 *
 * 认证由 TokenAuthenticationFilter 基于 token 参数完成，本过滤器需要在 Spring Security 过滤器之后执行。
 * 静态资源 {@link #STATIC_RESOURCE_PREFIX} 免登录，不经过本过滤器的校验
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class UReport2SecurityFilter extends OncePerRequestFilter {

    // DONE @AI：permissionconstants？新建一个类，把这个搞 过去；这样好维护点；项目里，或者 cms 模块里有例子的；已迁移到 PermissionConstants.UREPORT2_DESIGN，参照 IotPermissionConstants 的风格

    /**
     * UReport2 静态资源的 URL 前缀
     */
    public static final String STATIC_RESOURCE_PREFIX = UReportServlet.URL + "/res/ureport-asserts/";

    //  DONE @AI：全局有这个 header，可以使用的；tenant-id Header 已改为通过 WebFrameworkUtils#getTenantId 读取（即全局的 HEADER_TENANT_ID）。
    //  tenantId 是另一个字段：前端 iframe 地址上的 URL 参数，由 UReport2 自动附加到后续请求，项目中没有对应的全局参数常量，所以保留在这里；不能改成 tenant-id，否则会破坏已确认的 URL 协议
    /**
     * UReport2 透传的租户编号 URL 参数，与前端 report/ureport/index.vue 的 iframe 地址一致
     */
    private static final String PARAMETER_TENANT_ID = "tenantId";
    /**
     * UReport2 的报表文件 URL 参数
     */
    private static final String PARAMETER_FILE = "_u";

    /**
     * 查看权限可访问的 UReport2 Action，格式见 {@link #buildActionKey(HttpServletRequest)}
     *
     * 只包含已保存报表的预览、打印、导出，与 preview.bundle.js 的调用一致；其它的 Action（例如说 /designer、/datasource、/import 等）、
     * 新增的 Action，都需要设计权限
     */
    private static final Set<String> QUERY_ACTIONS = Set.of(
            // 预览、分页、打印
            "/preview", "/preview/loadData", "/preview/loadPrintPages", "/preview/loadPagePaper",
            // 导出 Excel
            "/excel", "/excel/paging", "/excel/sheet", "/excel97", "/excel97/paging", "/excel97/sheet",
            // 导出 PDF、PDF 在线打印
            "/pdf", "/pdf/show", "/pdf/newPaging",
            // 导出 Word
            "/word",
            // 导出时，回传预览页的图表图片
            "/chart/storeData");

    private final SecurityFrameworkService securityFrameworkService;
    /**
     * 租户 Service；关闭多租户时为 null
     */
    private final TenantFrameworkService tenantFrameworkService;
    private final GlobalExceptionHandler globalExceptionHandler;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI().substring(request.getContextPath().length());
        return uri.startsWith(STATIC_RESOURCE_PREFIX);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 1. 校验登录用户
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            ServletUtils.writeJSON(response, CommonResult.error(UNAUTHORIZED));
            return;
        }
        if (ObjUtil.notEqual(loginUser.getUserType(), UserTypeEnum.ADMIN.getValue())) {
            log.warn("[doFilterInternal][User({}/{}) 非管理后台用户，无法访问 URL({}/{})]", loginUser.getId(),
                    loginUser.getUserType(), request.getRequestURI(), request.getMethod());
            ServletUtils.writeJSON(response, CommonResult.error(FORBIDDEN));
            return;
        }

        // 保存原租户上下文，结束后恢复
        Long oldTenantId = TenantContextHolder.getTenantId();
        boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            // 2. 设置租户上下文
            if (tenantFrameworkService != null
                    && !setTenantContext(request, response, loginUser)) {
                return;
            }

            // 3. 校验 UReport2 权限：设计权限可访问全部请求；查看权限只能访问已保存报表的预览、打印、导出
            boolean hasPermission;
            try {
                hasPermission = securityFrameworkService.hasPermission(UREPORT2_DESIGN)
                        || (isQueryRequest(request) && securityFrameworkService.hasPermission(UREPORT2_QUERY));
            } catch (Throwable ex) {
                // 权限校验异常（例如说查询用户权限失败）时，不放行；只处理权限校验本身，后续过滤链的异常照常抛出
                writeExceptionResult(request, response, ex);
                return;
            }
            if (!hasPermission) {
                log.warn("[doFilterInternal][User({}) 没有 UReport2 权限({}/{})，无法访问 URL({}/{})]", loginUser.getId(),
                        UREPORT2_DESIGN, UREPORT2_QUERY, request.getRequestURI(), request.getMethod());
                ServletUtils.writeJSON(response, CommonResult.error(FORBIDDEN));
                return;
            }

            // 4. 继续过滤：为 UReport2 提供稳定的 Session 编号，原因见 UReport2SessionRequestWrapper 说明
            // DONE @AI：最好有个 identity 生成的方法？已抽取为 UReport2SessionRequestWrapper#buildIdentity
            chain.doFilter(new UReport2SessionRequestWrapper(request, loginUser), response);
        } finally {
            // 清理本次请求的模板缓存状态，包括模板不存在、解析失败等异常情况
            UReport2ReportDefinitionCache.clearPendingVersions();
            TenantContextHolder.setTenantId(oldTenantId);
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * 基于登录用户，设置租户上下文
     *
     * @param request 请求
     * @param response 响应
     * @param loginUser 登录用户
     * @return 是否设置成功；失败时，已写入响应
     */
    private boolean setTenantContext(HttpServletRequest request, HttpServletResponse response,
                                     LoginUser loginUser) {
        Long tenantId = loginUser.getTenantId();
        if (tenantId == null) {
            log.error("[setTenantContext][User({}/{}) 未关联租户，无法访问 URL({}/{})]", loginUser.getId(),
                    loginUser.getUserType(), request.getRequestURI(), request.getMethod());
            ServletUtils.writeJSON(response, CommonResult.error(BAD_REQUEST.getCode(), "请求的租户标识未传递，请进行排查"));
            return false;
        }
        // 2.1 tenant-id Header、tenantId 参数由客户端传递，只用于比对，避免越权问题；租户以登录用户为准
        Long headerTenantId = WebFrameworkUtils.getTenantId(request);
        String paramTenantId = request.getParameter(PARAMETER_TENANT_ID);
        if ((headerTenantId != null && ObjUtil.notEqual(headerTenantId, tenantId))
                || (StrUtil.isNotEmpty(paramTenantId) && ObjUtil.notEqual(paramTenantId, String.valueOf(tenantId)))) {
            log.error("[setTenantContext][租户({}) User({}/{}) 越权访问租户({}/{}) URL({}/{})]", tenantId,
                    loginUser.getId(), loginUser.getUserType(), headerTenantId, paramTenantId,
                    request.getRequestURI(), request.getMethod());
            ServletUtils.writeJSON(response, CommonResult.error(FORBIDDEN.getCode(), "您无权访问该租户的数据"));
            return false;
        }
        // 2.2 设置租户上下文
        TenantContextHolder.setTenantId(tenantId);
        TenantContextHolder.setIgnore(false);

        // 2.3 校验租户是合法，例如说被禁用、到期
        try {
            tenantFrameworkService.validTenant(tenantId);
        } catch (Throwable ex) {
            writeExceptionResult(request, response, ex);
            return false;
        }
        return true;
    }

    /**
     * 处理异常，并写入响应
     *
     * 异常日志使用 {@link UReport2ErrorLogRequestWrapper}，不记录 token、模板内容等请求参数及请求体
     *
     * @param request 请求
     * @param response 响应
     * @param ex 异常
     */
    private void writeExceptionResult(HttpServletRequest request, HttpServletResponse response, Throwable ex) {
        CommonResult<?> result = globalExceptionHandler.allExceptionHandler(new UReport2ErrorLogRequestWrapper(request), ex);
        ServletUtils.writeJSON(response, result);
    }

    /**
     * 判断是否为查看权限可访问的请求：{@link #QUERY_ACTIONS} 中的 Action，并且只访问已保存的报表
     *
     * @param request 请求
     * @return 是否可访问
     */
    private static boolean isQueryRequest(HttpServletRequest request) {
        String actionKey = buildActionKey(request);
        return actionKey != null && QUERY_ACTIONS.contains(actionKey)
                && isSavedReport(request.getParameter(PARAMETER_FILE));
    }

    /**
     * 按 UReportServlet 的分发规则，解析请求对应的 Action 与方法
     *
     * 1. 使用未解码的 requestURI 去掉 contextPath + /ureport 前缀，第一段为 Action 路径，剩余部分 trim 后为方法名，与 UReportServlet、BaseServletAction#retriveMethod 一致
     * 2. 方法名为空时，返回 Action 路径，对应 Action 的默认处理；否则，返回 Action 路径 + / + 方法名
     *
     * 编码、大小写、路径参数不同的请求，解析结果与 {@link #QUERY_ACTIONS} 不会精确匹配，需要设计权限
     *
     * @param request 请求
     * @return Action 与方法；不是 UReport2 请求时，返回 null
     */
    private static String buildActionKey(HttpServletRequest request) {
        String path = request.getContextPath() + UReportServlet.URL;
        String uri = request.getRequestURI();
        if (!uri.startsWith(path)) {
            return null;
        }
        String targetUrl = uri.substring(path.length());
        int slashPos = targetUrl.indexOf("/", 1);
        if (slashPos < 0) {
            return targetUrl;
        }
        String action = targetUrl.substring(0, slashPos);
        String method = targetUrl.substring(slashPos + 1).trim();
        return method.isEmpty() ? action : action + "/" + method;
    }

    /**
     * 判断 _u 参数是否为已保存的报表，即 {@link UReport2DatabaseReportProvider#PREFIX} 开头，区分大小写
     *
     * UReport2 的 Action 使用 URL 解码两次后的值加载报表（旧版本的 /pdf/newPaging 使用原值）；
     * 前缀不包含 % 和 +，URL 解码不会改变前缀，所以两种方式都只会从 {@link UReport2DatabaseReportProvider} 加载。
     * 设计器中未保存的报表（_u=p）、其它的报表来源，都不允许访问
     *
     * @param file _u 参数
     * @return 是否为已保存的报表
     */
    private static boolean isSavedReport(String file) {
        return StrUtil.startWith(file, UReport2DatabaseReportProvider.PREFIX);
    }

}
