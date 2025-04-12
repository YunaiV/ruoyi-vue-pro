package cn.iocoder.yudao.module.iot.script.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.script.context.DefaultScriptContext;
import cn.iocoder.yudao.module.iot.script.context.ScriptContext;
import cn.iocoder.yudao.module.iot.script.engine.JsScriptEngine;
import cn.iocoder.yudao.module.iot.script.engine.ScriptEngine;
import cn.iocoder.yudao.module.iot.script.engine.ScriptEngineFactory;
import cn.iocoder.yudao.module.iot.script.sandbox.JsSandbox;
import cn.iocoder.yudao.module.iot.script.sandbox.ScriptSandbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 脚本服务实现类
 */
@Slf4j
@Service
public class ScriptServiceImpl implements ScriptService {

    @Autowired
    private ScriptEngineFactory engineFactory;

    @Override
    public Object executeScript(String scriptType, String script, ScriptContext context) {
        if (StrUtil.isBlank(scriptType) || StrUtil.isBlank(script)) {
            return null;
        }

        ScriptEngine engine = engineFactory.getEngine(scriptType);
        if (engine == null) {
            log.error("找不到脚本引擎: {}", scriptType);
            throw new RuntimeException("不支持的脚本类型: " + scriptType);
        }

        try {
            return engine.execute(script, context);
        } catch (Exception e) {
            // TODO @haohao：最好打印一些参数；下面类似的也是
            log.error("执行脚本失败: {}", e.getMessage(), e);
            throw new RuntimeException("执行脚本失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Object executeScript(String scriptType, String script, Map<String, Object> params) {
        ScriptContext context = createContext(params);
        return executeScript(scriptType, script, context);
    }

    @Override
    public Object executeJavaScript(String script, ScriptContext context) {
        return executeScript(JsScriptEngine.TYPE, script, context);
    }

    @Override
    public Object executeJavaScript(String script, Map<String, Object> params) {
        return executeScript(JsScriptEngine.TYPE, script, params);
    }

    @Override
    public boolean validateScript(String scriptType, String script) {
        if (StrUtil.isBlank(scriptType) || StrUtil.isBlank(script)) {
            return true;
        }

        ScriptSandbox sandbox = getSandbox(scriptType);
        if (sandbox == null) {
            log.warn("找不到对应的脚本沙箱: {}", scriptType);
            return false;
        }

        try {
            return sandbox.validate(script);
        } catch (Exception e) {
            log.error("验证脚本安全性失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据脚本类型获取对应的沙箱实现
     *
     * @param scriptType 脚本类型
     * @return 沙箱实现
     */
    private ScriptSandbox getSandbox(String scriptType) {
        if (JsScriptEngine.TYPE.equals(scriptType)) {
            return new JsSandbox();
        }
        return null;
    }

    /**
     * 根据参数创建脚本上下文
     *
     * @param params 参数
     * @return 脚本上下文
     */
    private ScriptContext createContext(Map<String, Object> params) {
        ScriptContext context = new DefaultScriptContext();
        if (MapUtil.isNotEmpty(params)) {
            params.forEach(context::setParameter);
        }
        return context;
    }
}