package cn.iocoder.yudao.framework.sse.core.session;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SSE 服务端主动断开连接的原因枚举，通过 close 事件下发 {@link #name()}
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum SseCloseReasonEnum {

    REPLACED(false, "同一客户端重新连接，旧连接被替换"),
    CLIENT_CLOSE(false, "客户端主动关闭"),
    SHUTDOWN(true, "服务端停机，请重连其它节点"),
    TOKEN_INVALID(false, "访问令牌已失效，请重新登录");

    /**
     * 客户端是否应该自动重连
     */
    private final boolean reconnect;
    /**
     * 描述
     */
    private final String description;

}
