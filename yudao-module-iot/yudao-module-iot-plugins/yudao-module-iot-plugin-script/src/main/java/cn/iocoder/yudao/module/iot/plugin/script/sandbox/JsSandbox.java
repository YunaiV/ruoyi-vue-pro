package cn.iocoder.yudao.module.iot.plugin.script.sandbox;

import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptEngine;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * JavaScript脚本沙箱，限制脚本的执行权限
 */
@Slf4j
public class JsSandbox implements ScriptSandbox {

    /**
     * 禁止使用的关键字
     */
    private static final Set<String> FORBIDDEN_KEYWORDS = new HashSet<>(Arrays.asList(
            "java.lang.System", "java.io", "java.nio", "java.net", "javax.net",
            "java.security", "java.lang.reflect", "eval(", "Function(", "setTimeout",
            "setInterval", "exec(", "execSync"));

    /**
     * 正则表达式匹配禁止的关键字
     */
    private static final Pattern FORBIDDEN_PATTERN = Pattern.compile(
            "(?:import\\s+\\{\\s*.*\\s*\\}\\s+from)|" +
                    "(?:require\\s*\\()|" +
                    "(?:process\\.)|" +
                    "(?:globalThis\\.)|" +
                    "(?:\\bfs\\.)|" +
                    "(?:\\bchild_process\\b)|" +
                    "(?:\\bwindow\\b)");

    /**
     * 脚本执行超时时间（毫秒）
     */
    private static final long SCRIPT_TIMEOUT_MS = 5000;

    @Override
    public void applySandbox(Object engineContext, String script) {
        if (!(engineContext instanceof ScriptEngine)) {
            throw new IllegalArgumentException("引擎上下文类型不正确，无法应用JavaScript沙箱");
        }

        ScriptEngine engine = (ScriptEngine) engineContext;

        // 在Nashorn引擎中，可以通过以下方式设置安全限制
        try {
            // 设置严格模式
            String securityPrefix = "'use strict';\n";

            // 禁用Java.type等访问系统资源的功能
            engine.eval("var Java = undefined;");
            engine.eval("var JavaImporter = undefined;");
            engine.eval("var Packages = undefined;");

            // 增强安全控制可以在这里添加
            log.debug("已应用JavaScript安全沙箱限制");

        } catch (Exception e) {
            log.warn("应用JavaScript沙箱限制失败: {}", e.getMessage());
        }
    }

    @Override
    public boolean validateScript(String script) {
        if (script == null || script.isEmpty()) {
            return false;
        }

        // 检查禁止的关键字
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (script.contains(keyword)) {
                log.warn("脚本包含禁止使用的关键字: {}", keyword);
                return false;
            }
        }

        // 使用正则表达式检查更复杂的模式
        if (FORBIDDEN_PATTERN.matcher(script).find()) {
            log.warn("脚本包含禁止使用的模式");
            return false;
        }

        // 脚本长度限制
        if (script.length() > 1024 * 100) { // 限制100KB
            log.warn("脚本太大，超过了限制");
            return false;
        }

        return true;
    }
}