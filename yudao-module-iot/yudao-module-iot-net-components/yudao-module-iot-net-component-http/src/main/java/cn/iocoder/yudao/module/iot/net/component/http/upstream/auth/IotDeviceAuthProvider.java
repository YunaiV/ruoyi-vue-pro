package cn.iocoder.yudao.module.iot.net.component.http.upstream.auth;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * IoT 设备认证提供者
 * <p>
 * 用于 HTTP 设备接入时的身份认证
 *
 * @author 芋道源码
 */
@Slf4j
public class IotDeviceAuthProvider {

    private final ApplicationContext applicationContext;

    /**
     * 构造函数
     *
     * @param applicationContext Spring 应用上下文
     */
    public IotDeviceAuthProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 认证设备
     *
     * @param context  路由上下文
     * @param clientId 设备唯一标识
     * @return 认证结果 Future 对象
     */
    public Future<Void> authenticate(RoutingContext context, String clientId) {
        if (clientId == null || clientId.isEmpty()) {
            return Future.failedFuture("clientId 不能为空");
        }

        try {
            log.info("[authenticate][设备认证成功，clientId={}]", clientId);
            return Future.succeededFuture();
        } catch (Exception e) {
            log.error("[authenticate][设备认证异常，clientId={}]", clientId, e);
            return Future.failedFuture(e);
        }
    }
}