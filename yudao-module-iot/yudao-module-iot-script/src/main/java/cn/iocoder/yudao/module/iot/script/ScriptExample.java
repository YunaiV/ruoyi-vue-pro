package cn.iocoder.yudao.module.iot.script;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.script.context.DefaultScriptContext;
import cn.iocoder.yudao.module.iot.script.context.ScriptContext;
import cn.iocoder.yudao.module.iot.script.service.ScriptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 脚本使用示例类
 */
@Slf4j
@Component
public class ScriptExample {

    @Autowired
    private ScriptService scriptService;

    /**
     * 执行简单的 JavaScript 脚本
     *
     * @return 执行结果
     */
    public Object executeSimpleScript() {
        // 简单的脚本内容
        String script = "var result = a + b; result;";

        // 创建参数
        Map<String, Object> params = MapUtil.newHashMap();
        params.put("a", 10);
        params.put("b", 20);

        // 执行脚本
        return scriptService.executeJavaScript(script, params);
    }

    /**
     * 执行包含函数的 JavaScript 脚本
     *
     * @return 执行结果
     */
    public Object executeScriptWithFunction() {
        // 包含函数的脚本内容
        String script = "function calc(x, y) { return x * y; } calc(a, b);";

        // 创建上下文
        ScriptContext context = new DefaultScriptContext();
        context.setParameter("a", 5);
        context.setParameter("b", 6);

        // 执行脚本
        return scriptService.executeJavaScript(script, context);
    }

    /**
     * 执行包含工具类使用的脚本
     *
     * @return 执行结果
     */
    public Object executeScriptWithUtils() {
        // 使用工具类的脚本内容
        String script = "var data = {name: 'test', value: 123}; utils.toJson(data);";

        // 执行脚本
        return scriptService.executeJavaScript(script, MapUtil.newHashMap());
    }

    /**
     * 执行包含日志输出的脚本
     *
     * @return 执行结果
     */
    public Object executeScriptWithLogging() {
        // 包含日志输出的脚本内容
        String script = "log.info('脚本开始执行...'); " +
                "var result = a + b; " +
                "log.info('计算结果: ' + result); " +
                "result;";

        // 创建参数
        Map<String, Object> params = MapUtil.newHashMap();
        params.put("a", 100);
        params.put("b", 200);

        // 执行脚本
        return scriptService.executeJavaScript(script, params);
    }

    /**
     * 演示脚本安全性验证
     *
     * @return 是否安全
     */
    public boolean validateScriptSecurity() {
        // 安全的脚本
        String safeScript = "var x = 10; var y = 20; x + y;";
        boolean safeResult = scriptService.validateScript("js", safeScript);

        // 不安全的脚本
        String unsafeScript = "java.lang.System.exit(0);";
        boolean unsafeResult = scriptService.validateScript("js", unsafeScript);

        log.info("安全脚本验证结果: {}", safeResult);
        log.info("不安全脚本验证结果: {}", unsafeResult);

        return safeResult && !unsafeResult;
    }
}