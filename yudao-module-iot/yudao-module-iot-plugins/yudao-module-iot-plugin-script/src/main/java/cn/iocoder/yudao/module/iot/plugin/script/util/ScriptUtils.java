package cn.iocoder.yudao.module.iot.plugin.script.util;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

// TODO @haohao：【重要】 ScriptUtil.createGroovyEngine() 可以服用 hutool 的封装么？
// TODO @haohao：【重要】 js 引擎，可能要看下 jdk8 的兼容性；
// TODO @haohao：【重要】我们要不 script 配置的时候，支持 scriptType？！感觉会更通用一些？？？groovy、python、js
/**
 * 脚本工具类，提供执行脚本的辅助方法
 */
@Slf4j
public class ScriptUtils {

    /**
     * 默认脚本执行超时时间（毫秒）
     */
    private static final long DEFAULT_TIMEOUT_MS = 3000;

    /**
     * 脚本执行线程池
     */
    private static final ExecutorService SCRIPT_EXECUTOR = new ThreadPoolExecutor(
            2, 10, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            r -> new Thread(r, "script-executor-" + r.hashCode()),
            new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 带超时的执行任务
     *
     * @param task      任务
     * @param timeoutMs 超时时间（毫秒）
     * @param <T>       返回类型
     * @return 任务结果
     * @throws RuntimeException 执行异常
     */
    public static <T> T executeWithTimeout(Callable<T> task, long timeoutMs) {
        Future<T> future = SCRIPT_EXECUTOR.submit(task);
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("脚本执行超时，已终止");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("脚本执行被中断");
        } catch (ExecutionException e) {
            throw new RuntimeException("脚本执行失败: " + e.getCause().getMessage(), e.getCause());
        }
    }

    /**
     * 带默认超时的执行任务
     *
     * @param task 任务
     * @param <T>  返回类型
     * @return 任务结果
     * @throws RuntimeException 执行异常
     */
    public static <T> T executeWithTimeout(Callable<T> task) {
        return executeWithTimeout(task, DEFAULT_TIMEOUT_MS);
    }

    /**
     * 关闭工具类的线程池
     */
    public static void shutdown() {
        // TODO @芋艿：有没默认工具类，可以 shutdown
        SCRIPT_EXECUTOR.shutdown();
        try {
            if (!SCRIPT_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
                SCRIPT_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            SCRIPT_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // TODO @芋艿：要不要使用 JsonUtils
    /**
     * 将 JSON 字符串转换为 Map
     *
     * @param json JSON字符串
     * @return Map对象，转换失败则返回null
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseJson(String json) {
        try {
            return JSONUtil.toBean(json, Map.class);
        } catch (Exception e) {
            // TODO @haohao：json、e 都打印出来哈
            log.error("[parseJson][解析JSON失败: {}]", e.getMessage());
            return null;
        }
    }

    // TODO @芋艿：要不要封装成 utils
    /**
     * 尝试将对象转换为整数
     *
     * @param obj 需要转换的对象
     * @return 转换后的整数，如果无法转换则返回 null
     */
    public static Integer toInteger(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                log.debug("无法将字符串转换为整数: {}", obj);
                return null;
            }
        }

        log.debug("无法将对象转换为整数: {}", obj.getClass().getName());
        return null;
    }

    // TODO @芋艿：要不要封装成 utils
    /**
     * 尝试将对象转换为双精度浮点数
     *
     * @param obj 需要转换的对象
     * @return 转换后的双精度浮点数，如果无法转换则返回null
     */
    public static Double toDouble(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Double) {
            return (Double) obj;
        } else if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                log.debug("无法将字符串转换为双精度浮点数: {}", obj);
                return null;
            }
        }

        log.debug("无法将对象转换为双精度浮点数: {}", obj.getClass().getName());
        return null;
    }

    /**
     * 比较两个数值是否相等，忽略其具体类型
     *
     * @param a 第一个数值
     * @param b 第二个数值
     * @return 如果两个数值相等则返回true，否则返回false
     */
    public static boolean numbersEqual(Number a, Number b) {
        // TODO @haohao：NumberUtil.equals(1, 1D)
        if (a == null || b == null) {
            return a == b;
        }

        return Math.abs(a.doubleValue() - b.doubleValue()) < 0.0000001;
    }

}