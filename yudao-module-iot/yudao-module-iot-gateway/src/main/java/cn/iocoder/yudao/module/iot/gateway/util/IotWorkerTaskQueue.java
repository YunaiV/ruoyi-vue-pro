package cn.iocoder.yudao.module.iot.gateway.util;

import io.vertx.core.Context;
import io.vertx.core.Future;
import lombok.Getter;

import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

/**
 * 阻塞任务队列：任务在 Vert.x worker 上执行，结果回到所在的 context（event loop）
 * <ul>
 *     <li>有序（一个连接一个）：按提交顺序逐个执行、逐个完成（MQTT 的 PUBACK 顺序、TCP / WebSocket 的帧顺序）；
 *         不同连接各自一个队列，互不等待，并行使用 worker 池；</li>
 *     <li>无序（UDP、EMQX 这类没有连接的入口）：只限制排队数量，任务之间并行；</li>
 *     <li>有界：排队中的任务达到上限时拒绝，由调用方决定（例如关闭连接、丢弃）。</li>
 * </ul>
 * 只能在所在的 context 线程上调用（Vert.x 的处理器都在这个线程上），所以不需要加锁
 *
 * @author 芋道源码
 */
public class IotWorkerTaskQueue {

    /**
     * 默认每个连接最多排队的任务数
     */
    public static final int DEFAULT_MAX_PENDING = 256;

    @Getter
    private final Context context;
    private final boolean ordered;
    private final int maxPending;

    /**
     * 最后一个任务的结果：新任务排在它之后
     */
    private Future<?> tail = Future.succeededFuture();
    /**
     * 排队中（含执行中）的任务数
     */
    private int pending;
    /**
     * 是否已关闭：连接关闭后不再接收新任务
     */
    private boolean closed;

    /**
     * 创建一个连接的有序队列
     *
     * @param context 连接所在的 context
     */
    public IotWorkerTaskQueue(Context context) {
        this(context, true, DEFAULT_MAX_PENDING);
    }

    public IotWorkerTaskQueue(Context context, boolean ordered, int maxPending) {
        this.context = context;
        this.ordered = ordered;
        this.maxPending = maxPending;
    }

    /**
     * 提交一个阻塞任务：有序时排在之前的任务之后执行（前一个任务失败也照常执行）
     *
     * @param task 阻塞任务
     * @return 任务结果，在连接所在的 context 上完成；队列已满或已关闭时立即失败（{@link RejectedExecutionException}）
     */
    public <T> Future<T> submit(Callable<T> task) {
        if (closed) {
            return Future.failedFuture(new RejectedExecutionException("连接已关闭"));
        }
        if (pending >= maxPending) {
            return Future.failedFuture(new RejectedExecutionException("连接的待处理消息已达上限 " + maxPending));
        }
        return enqueue(task);
    }

    /**
     * 提交连接的最后一个任务（例如离线通知）：不受上限限制，之后不再接收新任务
     *
     * @param task 阻塞任务
     * @return 任务结果
     */
    public <T> Future<T> submitLast(Callable<T> task) {
        if (closed) {
            return Future.failedFuture(new RejectedExecutionException("连接已关闭"));
        }
        closed = true;
        return enqueue(task);
    }

    /**
     * 入队：计入排队数，交给 worker 执行；任务结束（成功或失败）时减去
     *
     * @param task 阻塞任务
     * @return 任务结果
     */
    private <T> Future<T> enqueue(Callable<T> task) {
        pending++;
        // 1. 无序：直接提交，任务之间并行
        if (!ordered) {
            Future<T> result;
            try {
                result = context.executeBlocking(task, false);
            } catch (RuntimeException e) { // Vertx 关闭时 worker 池会同步拒绝
                pending--;
                return Future.failedFuture(e);
            }
            return result.andThen(ar -> pending--);
        }
        // 2. 有序：排在上一个任务（tail）之后，上一个结束（无论成败）再提交；
        //    executeBlocking 的 ordered = false：同一个 context 上的其它连接不受影响；本连接的顺序由 tail 串起来
        Future<T> result = tail.transform(ignored -> context.<T>executeBlocking(task, false))
                .andThen(ar -> pending--);
        tail = result;
        return result;
    }

}
