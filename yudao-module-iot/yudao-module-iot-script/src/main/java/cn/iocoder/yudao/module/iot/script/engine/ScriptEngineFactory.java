package cn.iocoder.yudao.module.iot.script.engine;

import cn.iocoder.yudao.module.iot.script.sandbox.JsSandbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脚本引擎工厂，用于创建和缓存不同类型的脚本引擎，支持资源生命周期管理
 */
@Slf4j
@Component
public class ScriptEngineFactory implements DisposableBean {

    /**
     * 脚本引擎缓存
     */
    private final Map<String, ScriptEngine> engines = new ConcurrentHashMap<>();

    /**
     * 获取脚本引擎
     *
     * @param type 脚本类型
     * @return 脚本引擎
     */
    public ScriptEngine getEngine(String type) {
        // 从缓存中获取引擎
        return engines.computeIfAbsent(type, this::createEngine);
    }

    /**
     * 创建脚本引擎
     *
     * @param type 脚本类型
     * @return 脚本引擎
     */
    private ScriptEngine createEngine(String type) {
        try {
            if (JsScriptEngine.TYPE.equals(type)) {
                log.info("创建 GraalJS 脚本引擎");
                return new JsScriptEngine(new JsSandbox());
            }

            log.warn("不支持的脚本类型: {}", type);
            return null;
        } catch (Exception e) {
            log.error("创建脚本引擎 [{}] 失败: {}", type, e.getMessage(), e);
            throw new RuntimeException("创建脚本引擎失败: " + e.getMessage(), e);
        }
    }

    /**
     * 释放指定类型的引擎资源
     *
     * @param type 脚本类型
     */
    public void releaseEngine(String type) {
        ScriptEngine engine = engines.remove(type);
        if (engine instanceof AutoCloseable) {
            try {
                ((AutoCloseable) engine).close();
                log.info("已释放脚本引擎资源: {}", type);
            } catch (Exception e) {
                log.warn("释放脚本引擎 [{}] 资源时发生错误: {}", type, e.getMessage());
            }
        }
    }

    /**
     * 清理所有引擎资源
     */
    public void releaseAllEngines() {
        engines.keySet().forEach(this::releaseEngine);
        log.info("已清理所有脚本引擎资源");
    }

    @Override
    public void destroy() {
        // 应用关闭时，释放所有引擎资源
        log.info("应用关闭，释放所有脚本引擎资源...");
        releaseAllEngines();
    }
}