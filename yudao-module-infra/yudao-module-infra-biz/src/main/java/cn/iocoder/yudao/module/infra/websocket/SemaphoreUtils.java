package cn.iocoder.yudao.module.infra.websocket;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;

/**
 * 信号量相关处理
 *
 */
@Slf4j
public class SemaphoreUtils {

    /**
     * 获取信号量
     *
     * @param semaphore
     * @return
     */
    public static boolean tryAcquire(Semaphore semaphore) {
        boolean flag = false;

        try {
            flag = semaphore.tryAcquire();
        } catch (Exception e) {
            log.error("获取信号量异常", e);
        }

        return flag;
    }

    /**
     * 释放信号量
     *
     * @param semaphore
     */
    public static void release(Semaphore semaphore) {

        try {
            semaphore.release();
        } catch (Exception e) {
            log.error("释放信号量异常", e);
        }
    }
}
