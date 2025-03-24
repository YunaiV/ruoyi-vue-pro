package cn.iocoder.yudao.module.iot.plugin.script.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 脚本引擎工厂，用于创建不同类型的脚本引擎
 */
@Component
public class ScriptEngineFactory {
    private static final Logger logger = LoggerFactory.getLogger(ScriptEngineFactory.class);

    /**
     * 创建JavaScript脚本引擎
     *
     * @return JavaScript脚本引擎
     */
    public JsScriptEngine createJsEngine() {
        logger.debug("创建JavaScript脚本引擎");
        return new JsScriptEngine();
    }

    /**
     * 根据脚本类型创建对应的脚本引擎
     *
     * @param scriptType 脚本类型
     * @return 脚本引擎
     */
    public AbstractScriptEngine createEngine(String scriptType) {
        if (scriptType == null || scriptType.isEmpty()) {
            throw new IllegalArgumentException("脚本类型不能为空");
        }

        switch (scriptType.toLowerCase()) {
            case "js":
            case "javascript":
                return createJsEngine();
            // 可以在这里添加其他类型的脚本引擎
            default:
                throw new IllegalArgumentException("不支持的脚本类型: " + scriptType);
        }
    }
} 