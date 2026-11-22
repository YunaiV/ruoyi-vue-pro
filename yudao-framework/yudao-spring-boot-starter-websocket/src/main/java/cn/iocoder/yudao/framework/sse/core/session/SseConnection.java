package cn.iocoder.yudao.framework.sse.core.session;

import cn.hutool.core.util.IdUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * SSE 连接
 *
 * 封装 {@link SseEmitter}、登录用户信息、发送锁
 *
 * @author 芋道源码
 */
@Slf4j
@Getter
public class SseConnection {

    /**
     * 连接编号的前缀，用于和 WebSocket 的 sessionId 区分
     */
    public static final String CONNECTION_ID_PREFIX = "sse:";

    /**
     * 发送结果
     */
    public enum SendResult {

        /**
         * 发送成功
         */
        SUCCESS,
        /**
         * 等待发送锁超时，跳过本条
         */
        SKIPPED,
        /**
         * 发送失败，连接已不可用
         */
        FAILED

    }

    private final String connectionId;
    private final SseEmitter emitter;

    private final Long tenantId;
    private final Integer userType;
    private final Long userId;
    private final String clientId;
    /**
     * 访问令牌的过期时间，心跳时检查
     */
    private final LocalDateTime expiresTime;
    private final long createTime;

    /**
     * 最近一次发送成功的时间，单位：毫秒
     */
    private volatile long lastActiveTime;

    /**
     * 发送锁：SseEmitter 的 send 不是线程安全的，同一连接串行发送，避免帧交错
     */
    @Getter(lombok.AccessLevel.NONE)
    private final ReentrantLock sendLock = new ReentrantLock();
    /**
     * 连续发送失败（含等待锁超时）的次数
     */
    @Getter(lombok.AccessLevel.NONE)
    private final AtomicInteger sendFailures = new AtomicInteger();
    /**
     * 是否已关闭
     */
    @Getter(lombok.AccessLevel.NONE)
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * 服务端主动关闭时的处理，由 {@link SseEmitterSessionManager} 设置
     */
    @Getter(lombok.AccessLevel.NONE)
    private volatile Consumer<SseCloseReasonEnum> closeHandler;

    public SseConnection(SseEmitter emitter, Long tenantId, Integer userType, Long userId,
                         String clientId, LocalDateTime expiresTime) {
        this.connectionId = CONNECTION_ID_PREFIX + IdUtil.fastSimpleUUID();
        this.emitter = emitter;
        this.tenantId = tenantId;
        this.userType = userType;
        this.userId = userId;
        this.clientId = clientId;
        this.expiresTime = expiresTime;
        this.createTime = System.currentTimeMillis();
        this.lastActiveTime = this.createTime;
    }

    public String getUserKey() {
        return buildUserKey(tenantId, userType, userId);
    }

    /**
     * 构建用户标识，格式为 tenantId:userType:userId
     *
     * @param tenantId 租户编号，为空时使用 0
     * @param userType 用户类型
     * @param userId 用户编号
     * @return 用户标识
     */
    public static String buildUserKey(Long tenantId, Integer userType, Long userId) {
        return (tenantId != null ? tenantId : 0L) + ":" + userType + ":" + userId;
    }

    /**
     * 发送事件
     *
     * @param event 事件
     * @param timeoutMillis 等待发送锁的超时时间，单位：毫秒
     * @return 发送结果
     */
    public SendResult send(SseEmitter.SseEventBuilder event, long timeoutMillis) {
        if (closed.get()) {
            return SendResult.FAILED;
        }
        if (!tryLock(timeoutMillis)) {
            sendFailures.incrementAndGet();
            return SendResult.SKIPPED;
        }
        try {
            // 获得锁后再次检查：等待锁期间连接可能已关闭，保证 close 事件之后不会再发送其他事件
            if (closed.get()) {
                return SendResult.FAILED;
            }
            emitter.send(event);
            lastActiveTime = System.currentTimeMillis();
            sendFailures.set(0);
            return SendResult.SUCCESS;
        } catch (Exception ex) { // IOException、IllegalStateException（已完成）等，均视为连接不可用
            log.debug("[send][连接({}) 发送失败]", connectionId, ex);
            return SendResult.FAILED;
        } finally {
            sendLock.unlock();
        }
    }

    /**
     * 发送最后一个事件（例如说 close 事件）并结束 emitter，调用前需先 {@link #markClosed()}
     *
     * 与 {@link #send(SseEmitter.SseEventBuilder, long)} 共用发送锁，保证最后一个事件之后不会再发送其他事件；
     * 等待发送锁超时时，不再发送最后一个事件，直接结束 emitter
     *
     * @param event 事件
     * @param timeoutMillis 等待发送锁的超时时间，单位：毫秒
     */
    public void closeWithLastEvent(SseEmitter.SseEventBuilder event, long timeoutMillis) {
        boolean locked = tryLock(timeoutMillis);
        try {
            if (locked) {
                try {
                    emitter.send(event);
                } catch (Exception ex) {
                    log.debug("[closeWithLastEvent][连接({}) 发送最后一个事件失败]", connectionId, ex);
                }
            }
            emitter.complete();
        } finally {
            if (locked) {
                sendLock.unlock();
            }
        }
    }

    private boolean tryLock(long timeoutMillis) {
        try {
            return sendLock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * @return 连续发送失败的次数
     */
    public int getSendFailures() {
        return sendFailures.get();
    }

    /**
     * 标记为已关闭
     *
     * @return 是否首次标记
     */
    public boolean markClosed() {
        return closed.compareAndSet(false, true);
    }

    public boolean isClosed() {
        return closed.get();
    }

    void setCloseHandler(Consumer<SseCloseReasonEnum> closeHandler) {
        this.closeHandler = closeHandler;
    }

    /**
     * 服务端主动关闭连接
     *
     * @param reason 关闭原因
     */
    public void close(SseCloseReasonEnum reason) {
        Consumer<SseCloseReasonEnum> handler = this.closeHandler;
        if (handler != null) {
            handler.accept(reason);
        }
    }

}
