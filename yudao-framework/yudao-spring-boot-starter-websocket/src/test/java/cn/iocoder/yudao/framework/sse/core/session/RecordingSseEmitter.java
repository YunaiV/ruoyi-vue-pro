package cn.iocoder.yudao.framework.sse.core.session;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 记录发送内容的 {@link SseEmitter}，用于单元测试
 *
 * @author 芋道源码
 */
public class RecordingSseEmitter extends SseEmitter {

    public final List<String> frames = new CopyOnWriteArrayList<>();
    public final AtomicInteger concurrentSends = new AtomicInteger();
    public volatile boolean overlapped;
    public volatile boolean completed;
    public volatile Throwable error;
    public volatile IOException failure;
    public volatile long sendDelayMillis;
    /**
     * 不为空时，send 会等待其放行后再记录，用于模拟发送中的帧
     */
    public volatile CountDownLatch sendBlocker;

    public RecordingSseEmitter() {
        super(60_000L);
    }

    @Override
    public void send(SseEventBuilder builder) throws IOException {
        if (concurrentSends.incrementAndGet() > 1) {
            overlapped = true;
        }
        try {
            if (failure != null) {
                throw failure;
            }
            CountDownLatch blocker = sendBlocker;
            if (blocker != null) {
                blocker.await(5, TimeUnit.SECONDS);
            }
            if (sendDelayMillis > 0) {
                Thread.sleep(sendDelayMillis);
            }
            frames.add(render(builder));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            concurrentSends.decrementAndGet();
        }
    }

    @Override
    public synchronized void complete() {
        completed = true;
        super.complete();
    }

    @Override
    public synchronized void completeWithError(Throwable ex) {
        completed = true;
        error = ex;
        super.completeWithError(ex);
    }

    public static String render(SseEventBuilder builder) {
        StringBuilder sb = new StringBuilder();
        for (ResponseBodyEmitter.DataWithMediaType data : builder.build()) {
            sb.append(data.getData());
        }
        return sb.toString();
    }

    public long countFrames(String prefix) {
        return frames.stream().filter(frame -> frame.contains(prefix)).count();
    }

}
