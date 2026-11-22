package cn.iocoder.yudao.framework.sse.core.scheduler;

import cn.iocoder.yudao.framework.sse.core.codec.SseEventEncoder;
import cn.iocoder.yudao.framework.sse.core.session.SseCloseReasonEnum;
import cn.iocoder.yudao.framework.sse.core.session.SseConnection;
import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSE 心跳调度器
 *
 * 1. 心跳：独立的单线程调度器，按周期向全部连接发送注释帧 : ping
 * 2. 令牌过期：访问令牌过期的连接，发送 close: TOKEN_INVALID 后断开
 * 3. 兜底扫描：超过 3 个心跳周期没有发送成功的连接，视为僵尸连接并清理
 * 4. 优雅停机：先于 Web 服务器的优雅停机执行，向全部连接发送 close: SHUTDOWN，避免长连接阻塞停机
 *
 * @author 芋道源码
 */
@Slf4j
public class SseHeartbeatScheduler implements SmartLifecycle {

    private static final String THREAD_NAME = "yudao-sse-heartbeat-";
    /**
     * 僵尸连接的判定倍数：超过 N 个心跳周期没有发送成功
     */
    private static final int ZOMBIE_HEARTBEAT_TIMES = 3;

    private final SseEmitterSessionManager sessionManager;

    private final long heartbeatIntervalMillis;
    private final long sendTimeoutMillis;

    private ScheduledExecutorService executor;
    private volatile boolean running;

    public SseHeartbeatScheduler(SseEmitterSessionManager sessionManager,
                                 long heartbeatIntervalMillis, long sendTimeoutMillis) {
        this.sessionManager = sessionManager;
        this.heartbeatIntervalMillis = heartbeatIntervalMillis;
        this.sendTimeoutMillis = sendTimeoutMillis;
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor(ThreadUtil.newNamedThreadFactory(THREAD_NAME, true));
        executor.scheduleWithFixedDelay(this::heartbeat, heartbeatIntervalMillis, heartbeatIntervalMillis, TimeUnit.MILLISECONDS);
        running = true;
    }

    @Override
    public synchronized void stop() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        sessionManager.shutdown();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 使用最大的 phase，保证最先停止，早于 Web 服务器的优雅停机
     */
    @Override
    public int getPhase() {
        return SmartLifecycle.DEFAULT_PHASE;
    }

    /**
     * 执行一轮心跳
     */
    public void heartbeat() {
        long now = System.currentTimeMillis();
        LocalDateTime nowTime = LocalDateTime.now();
        List<SseConnection> connections = new ArrayList<>(sessionManager.getConnections());
        for (SseConnection connection : connections) {
            try {
                heartbeat(connection, now, nowTime);
            } catch (Throwable ex) {
                log.error("[heartbeat][连接({}) 心跳异常]", connection.getConnectionId(), ex);
            }
        }
    }

    /**
     * 处理单个连接：令牌过期则关闭，长时间未活跃则清理，否则发送心跳
     *
     * @param connection 连接
     * @param now 当前时间戳，单位：毫秒
     * @param nowTime 当前时间，用于比较令牌过期时间
     */
    private void heartbeat(SseConnection connection, long now, LocalDateTime nowTime) {
        // 1. 访问令牌已过期，断开连接
        if (connection.getExpiresTime() != null && nowTime.isAfter(connection.getExpiresTime())) {
            sessionManager.close(connection, SseCloseReasonEnum.TOKEN_INVALID);
            return;
        }
        // 2. 兜底扫描：长时间没有发送成功，视为僵尸连接
        if (now - connection.getLastActiveTime() > heartbeatIntervalMillis * ZOMBIE_HEARTBEAT_TIMES) {
            log.warn("[heartbeat][连接({}) 超过 {} 个心跳周期未活跃，清理连接]", connection.getConnectionId(), ZOMBIE_HEARTBEAT_TIMES);
            sessionManager.abort(connection, null);
            return;
        }
        // 3. 发送心跳：失败说明客户端已断开
        SseConnection.SendResult result = connection.send(SseEventEncoder.encodeHeartbeat(), sendTimeoutMillis);
        if (result == SseConnection.SendResult.FAILED) {
            log.info("[heartbeat][连接({}) 心跳发送失败，移除连接]", connection.getConnectionId());
            sessionManager.abort(connection, null);
        }
    }

}
