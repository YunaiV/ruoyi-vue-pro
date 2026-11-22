package cn.iocoder.yudao.framework.sse.core.controller;

import cn.iocoder.yudao.framework.sse.core.session.SseCloseReasonEnum;
import cn.iocoder.yudao.framework.sse.core.session.SseConnection;
import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import cn.iocoder.yudao.framework.sse.core.codec.SseEventEncoder;
import cn.iocoder.yudao.framework.sse.core.codec.SseResponseHeaderSupport;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.sse.config.SseProperties;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * SSE 连接的 Controller
 *
 * 1. 路径与 WebSocket 的 /infra/ws 一样不带 /admin-api、/app-api 前缀，管理端和用户端共用；需要登录，由 TokenAuthenticationFilter 认证，
 *    token 优先从 Authorization 请求头读取，兼容 ?token= 参数（原生 EventSource 无法设置请求头）
 * 2. userType、tenantId 只取自 LoginUser，忽略请求参数和 tenant-id 请求头，防止跨租户订阅
 * 3. 只在 yudao.sse.enable=true 时注册
 *
 * @author 芋道源码
 */
@RestController
@RequestMapping("${yudao.sse.path:/infra/sse}")
@RequiredArgsConstructor
@Slf4j
public class SseController {

    private static final String HEADER_LAST_EVENT_ID = "Last-Event-ID";
    /**
     * 客户端编号的格式：1-64 位字母、数字、下划线、中划线
     */
    private static final String CLIENT_ID_REGEX = "^[A-Za-z0-9_-]{1,64}$";

    private final SseEmitterSessionManager sessionManager;

    private final SseProperties sseProperties;

    /**
     * 建立 SSE 连接
     *
     * @param clientId 客户端编号，由前端为每个标签页生成；同一客户端重连时替换旧连接
     * @param lastEventId 浏览器重连时携带的最后事件编号，v1 只记录日志，不补发
     */
    @GetMapping("/connect")
    public ResponseEntity<SseEmitter> connect(@RequestParam(value = "clientId", required = false) String clientId,
                                              @RequestHeader(value = HEADER_LAST_EVENT_ID, required = false) String lastEventId,
                                              HttpServletRequest request) {
        // 1. 校验登录用户
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null || loginUser.getUserType() == null || loginUser.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (StrUtil.isNotEmpty(clientId) && !clientId.matches(CLIENT_ID_REGEX)) {
            return ResponseEntity.badRequest().build();
        }

        // 2.1 创建连接
        SseEmitter emitter = new SseEmitter(sseProperties.getEmitterTimeout().toMillis());
        SseConnection connection = new SseConnection(emitter, loginUser.getTenantId(), loginUser.getUserType(),
                loginUser.getId(), StrUtil.emptyToNull(clientId), loginUser.getExpiresTime());
        // 2.2 生命周期回调：完成、超时、异常，统一标记关闭并幂等移除
        String connectionId = connection.getConnectionId();
        emitter.onCompletion(() -> {
            connection.markClosed();
            sessionManager.remove(connection);
        });
        emitter.onTimeout(() -> {
            log.debug("[connect][连接({}) 超时，等待客户端重连]", connectionId);
            sessionManager.abort(connection, null);
        });
        emitter.onError(ex -> {
            log.debug("[connect][连接({}) 异常]", connectionId, ex);
            connection.markClosed();
            sessionManager.remove(connection);
        });
        // 2.3 先发送连接成功事件：响应返回前的发送会先缓存，保证 connected 是第一帧，早于注册后投递的业务事件
        SseConnection.SendResult result = connection.send(SseEventEncoder.encodeConnected(connectionId,
                        sseProperties.getHeartbeatInterval().toMillis(), sseProperties.getRetryInterval().toMillis()),
                sseProperties.getSendTimeout().toMillis());
        if (result != SseConnection.SendResult.SUCCESS) {
            connection.markClosed();
            emitter.complete();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        // 3. 注册连接：已停机时返回 503
        if (!sessionManager.register(connection)) {
            log.warn("[connect][用户({}) 连接被拒绝，服务停机中，请求({})]",
                    connection.getUserKey(), maskToken(request));
            connection.markClosed();
            emitter.complete();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        log.info("[connect][连接({}) 用户({}) 客户端({}) 已建立，Last-Event-ID({})]",
                connectionId, connection.getUserKey(), connection.getClientId(), lastEventId);
        return ResponseEntity.ok().headers(SseResponseHeaderSupport.buildHeaders(request)).body(emitter);
    }

    /**
     * 主动关闭 SSE 连接
     *
     * 只能关闭当前登录用户自己的连接。集群部署时，请求可能落到其它节点，此时返回 false，前端仍需在本地中止连接
     *
     * @param connectionId 连接编号
     */
    @PostMapping("/close")
    public CommonResult<Boolean> close(@RequestParam("connectionId") String connectionId) {
        // 1. 校验登录用户
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return CommonResult.error(GlobalErrorCodeConstants.UNAUTHORIZED);
        }
        // 2.1 连接不在本节点：返回 false，由前端自行中止
        SseConnection connection = sessionManager.getConnection(connectionId);
        if (connection == null) {
            return success(false);
        }
        // 2.2 只能关闭自己的连接
        if (ObjUtil.notEqual(connection.getTenantId(), loginUser.getTenantId())
                || ObjUtil.notEqual(connection.getUserType(), loginUser.getUserType())
                || ObjUtil.notEqual(connection.getUserId(), loginUser.getId())) {
            log.warn("[close][用户({}/{}/{}) 尝试关闭他人的连接({})]", loginUser.getTenantId(),
                    loginUser.getUserType(), loginUser.getId(), connectionId);
            return CommonResult.error(GlobalErrorCodeConstants.FORBIDDEN);
        }

        // 3. 关闭连接
        sessionManager.close(connection, SseCloseReasonEnum.CLIENT_CLOSE);
        return success(true);
    }

    /**
     * 脱敏请求地址中的 token 参数，避免令牌进入日志
     *
     * @param request 请求
     * @return 脱敏后的请求地址
     */
    static String maskToken(HttpServletRequest request) {
        String query = request.getQueryString();
        if (StrUtil.isEmpty(query)) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + maskTokenQuery(query);
    }

    static String maskTokenQuery(String query) {
        return query.replaceAll("(?i)(^|&)(token|access_token)=[^&]*", "$1$2=***");
    }

}
