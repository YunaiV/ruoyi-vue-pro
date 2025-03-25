package cn.iocoder.yudao.module.iot.plugin.script.service;

import cn.iocoder.yudao.module.iot.plugin.script.context.PluginScriptContext;
import cn.iocoder.yudao.module.iot.plugin.script.context.ScriptContext;
import cn.iocoder.yudao.module.iot.plugin.script.engine.AbstractScriptEngine;
import cn.iocoder.yudao.module.iot.plugin.script.engine.ScriptEngineFactory;
import cn.iocoder.yudao.module.iot.plugin.script.sandbox.JsSandbox;
import cn.iocoder.yudao.module.iot.plugin.script.sandbox.ScriptSandbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脚本服务实现类
 */
@Service
@Slf4j
public class ScriptServiceImpl implements ScriptService {

    @Resource
    private ScriptEngineFactory engineFactory;

    /**
     * 脚本引擎缓存，避免重复创建
     */
    private final Map<String, AbstractScriptEngine> engineCache = new ConcurrentHashMap<>();

    /**
     * 脚本沙箱缓存
     */
    private final Map<String, ScriptSandbox> sandboxCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 初始化常用的脚本引擎和沙箱
        // TODO @haohao：js 是不是要枚举下哈。
        getEngine("js");
        sandboxCache.put("js", new JsSandbox());
    }

    @PreDestroy
    public void destroy() {
        // 销毁所有引擎
        for (AbstractScriptEngine engine : engineCache.values()) {
            try {
                engine.destroy();
            } catch (Exception e) {
                // TODO @haohao：engine 类名
                log.error("销毁脚本引擎失败", e);
            }
        }
        engineCache.clear();
        sandboxCache.clear();
    }

    @Override
    public Object executeScript(String scriptType, String script, ScriptContext context) {
        // TODO @haohao：可以使用 hutool assert
        if (scriptType == null || script == null) {
            throw new IllegalArgumentException("脚本类型和内容不能为空");
        }

        // 获取脚本引擎
        AbstractScriptEngine engine = getEngine(scriptType);

        // 验证脚本是否安全
        if (!validateScript(scriptType, script)) {
            throw new SecurityException("脚本包含不安全的代码，无法执行");
        }

        try {
            // 执行脚本
            return engine.execute(script, context);
        } catch (Exception e) {
            // TODO @haohao：最好把 e 堆栈出来哈；然后，engine 类名
            log.error("执行脚本失败: {}", e.getMessage());
            throw new RuntimeException("执行脚本失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Object executeScript(String scriptType, String script, Map<String, Object> params) {
        // 创建默认上下文
        ScriptContext context = new PluginScriptContext(params);
        // 执行脚本
        return executeScript(scriptType, script, context);
    }

    @Override
    public Object executeJavaScript(String script, ScriptContext context) {
        // TODO @haohao：枚举哈
        return executeScript("js", script, context);
    }

    @Override
    public Object executeJavaScript(String script, Map<String, Object> params) {
        // TODO @haohao：枚举哈
        return executeScript("js", script, params);
    }

    @Override
    public boolean validateScript(String scriptType, String script) {
        ScriptSandbox sandbox = sandboxCache.get(scriptType.toLowerCase());
        if (sandbox == null) {
            // TODO @haohao：疑问，为啥默认 JsSandbox 哈？
            log.warn("[validateScript][找不到脚本类型[{}]对应的沙箱，使用默认 JS 沙箱]", scriptType);
            sandbox = new JsSandbox();
            sandboxCache.put(scriptType.toLowerCase(), sandbox);
        }
        return sandbox.validateScript(script);
    }

    /**
     * 获取脚本引擎，如果不存在则创建
     *
     * @param scriptType 脚本类型
     * @return 脚本引擎
     */
    private AbstractScriptEngine getEngine(String scriptType) {
        return engineCache.computeIfAbsent(scriptType.toLowerCase(), type -> {
            AbstractScriptEngine engine = engineFactory.createEngine(type);
            engine.init();
            return engine;
        });
    }
}