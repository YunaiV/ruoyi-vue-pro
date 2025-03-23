package cn.iocoder.yudao.module.iot.plugin.script.engine;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.plugin.script.context.ScriptContext;
import cn.iocoder.yudao.module.iot.plugin.script.sandbox.JsSandbox;
import cn.iocoder.yudao.module.iot.plugin.script.util.ScriptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JavaScript脚本引擎实现
 * 使用JSR-223 Nashorn脚本引擎
 */
public class JsScriptEngine extends AbstractScriptEngine {
    private static final Logger logger = LoggerFactory.getLogger(JsScriptEngine.class);

    /**
     * 默认脚本执行超时时间（毫秒）
     */
    private static final long DEFAULT_TIMEOUT_MS = 5000;

    /**
     * JavaScript引擎名称
     */
    private static final String JS_ENGINE_NAME = "nashorn";

    /**
     * 脚本引擎管理器
     */
    private ScriptEngineManager engineManager;

    /**
     * 脚本引擎实例
     */
    private ScriptEngine engine;

    /**
     * 脚本缓存
     */
    private final Map<String, Object> cachedScripts = new ConcurrentHashMap<>();

    @Override
    public void init() {
        logger.info("初始化JavaScript脚本引擎");

        // 创建脚本引擎管理器
        engineManager = new ScriptEngineManager();

        // 获取JavaScript引擎
        engine = engineManager.getEngineByName(JS_ENGINE_NAME);
        if (engine == null) {
            logger.error("无法创建JavaScript引擎，尝试使用JavaScript名称获取");
            engine = engineManager.getEngineByName("JavaScript");
        }

        if (engine == null) {
            throw new IllegalStateException("无法创建JavaScript引擎，请检查环境配置");
        }

        logger.info("成功创建JavaScript引擎: {}", engine.getClass().getName());

        // 默认使用JS沙箱
        if (sandbox == null) {
            setSandbox(new JsSandbox());
        }
    }

    @Override
    public Object execute(String script, ScriptContext context) {
        if (engine == null) {
            init();
        }

        // 创建可超时执行的任务
        Callable<Object> task = () -> {
            try {
                // 创建脚本绑定
                Bindings bindings = new SimpleBindings();
                if (context != null) {
                    // 添加上下文参数
                    Map<String, Object> contextParams = context.getParameters();
                    if (MapUtil.isNotEmpty(contextParams)) {
                        bindings.putAll(contextParams);
                    }

                    // 添加上下文函数
                    bindings.putAll(context.getFunctions());
                }

                // 应用沙箱限制
                if (sandbox != null) {
                    sandbox.applySandbox(engine, script);
                }

                // 执行脚本
                return engine.eval(script, bindings);
            } catch (ScriptException e) {
                logger.error("执行JavaScript脚本异常: {}", e.getMessage());
                throw new RuntimeException("脚本执行异常: " + e.getMessage(), e);
            }
        };

        try {
            // 使用超时执行器执行脚本
            return ScriptUtils.executeWithTimeout(task, DEFAULT_TIMEOUT_MS);
        } catch (Exception e) {
            logger.error("执行JavaScript脚本错误: {}", e.getMessage());
            throw new RuntimeException("脚本执行失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Object execute(String script, Map<String, Object> params) {
        if (engine == null) {
            init();
        }

        // 创建可超时执行的任务
        Callable<Object> task = () -> {
            try {
                // 创建脚本绑定
                Bindings bindings = new SimpleBindings();
                if (MapUtil.isNotEmpty(params)) {
                    bindings.putAll(params);
                }

                // 应用沙箱限制
                if (sandbox != null) {
                    sandbox.applySandbox(engine, script);
                }

                // 执行脚本
                return engine.eval(script, bindings);
            } catch (ScriptException e) {
                logger.error("执行JavaScript脚本异常: {}", e.getMessage());
                throw new RuntimeException("脚本执行异常: " + e.getMessage(), e);
            }
        };

        try {
            // 使用超时执行器执行脚本
            return ScriptUtils.executeWithTimeout(task, DEFAULT_TIMEOUT_MS);
        } catch (Exception e) {
            logger.error("执行JavaScript脚本错误: {}", e.getMessage());
            throw new RuntimeException("脚本执行失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
        logger.info("销毁JavaScript脚本引擎");
        cachedScripts.clear();
        engine = null;
        engineManager = null;
    }
}