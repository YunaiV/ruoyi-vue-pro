package cn.iocoder.yudao.module.iot.script.sandbox;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JavaScript 沙箱实现，提供脚本安全性验证
 */
@Slf4j
public class JsSandbox implements ScriptSandbox {

    /**
     * JavaScript 沙箱类型
     */
    public static final String TYPE = "js";

    /**
     * 不安全的关键字列表
     */
    private final List<String> unsafeKeywords = new ArrayList<>();

    /**
     * 可能导致高资源消耗的关键字
     */
    private final List<String> highResourceKeywords = new ArrayList<>();

    /**
     * 不安全的包/类访问模式
     */
    private final List<Pattern> unsafePatterns = new ArrayList<>();

    /**
     * 递归或循环嵌套深度检测模式
     */
    private final List<Pattern> recursionPatterns = new ArrayList<>();

    /**
     * 允许的脚本最大长度（字节）
     */
    private static final int MAX_SCRIPT_LENGTH = 100 * 1024; // 100KB

    /**
     * 脚本安全验证超时时间（毫秒）
     */
    private static final long VALIDATION_TIMEOUT = 1000; // 1秒

    /**
     * 构造函数，初始化不安全的关键字和模式
     */
    public JsSandbox() {
        // 初始化 Java 相关的不安全关键字
        Arrays.asList(
                "java.lang.System",
                "java.io",
                "java.net",
                "java.nio",
                "java.security",
                "java.rmi",
                "java.lang.reflect",
                "java.sql",
                "javax.sql",
                "javax.naming",
                "javax.script",
                "javax.tools",
                "org.omg",
                "org.graalvm.polyglot",
                "sun.",
                "javafx.",
                "Packages.",
                "com.sun.",
                "com.oracle.").forEach(unsafeKeywords::add);

        // GraalJS 特有的不安全关键字
        Arrays.asList(
                "Polyglot.import",
                "Polyglot.eval",
                "Java.type",
                "allowHostAccess",
                "allowNativeAccess",
                "allowIO",
                "allowHostClassLoading",
                "allowAllAccess",
                "allowExperimentalOptions",
                "Context.Builder",
                "Context.create",
                "Context.getCurrent",
                "Context.newBuilder",
                "__proto__",
                "__defineGetter__",
                "__defineSetter__",
                "__lookupGetter__",
                "__lookupSetter__",
                "__noSuchMethod__",
                "constructor.constructor",
                "Object.constructor").forEach(unsafeKeywords::add);

        // 可能导致高资源消耗的关键字
        Arrays.asList(
                "while(true)",
                "for(;;)",
                "do{",
                "BigInt",
                "Promise.all",
                "setTimeout",
                "setInterval",
                "new Array(",
                "Array(",
                "new ArrayBuffer(",
                ".repeat(",
                ".forEach(",
                ".map(",
                ".reduce(").forEach(highResourceKeywords::add);

        // 初始化不安全的模式
        // 系统访问和进程执行
        unsafePatterns.add(Pattern.compile("java\\.lang\\.Runtime"));
        unsafePatterns.add(Pattern.compile("java\\.lang\\.ProcessBuilder"));
        unsafePatterns.add(Pattern.compile("java\\.lang\\.reflect"));

        // 特殊对象和操作
        unsafePatterns.add(Pattern.compile("Packages"));
        unsafePatterns.add(Pattern.compile("JavaImporter"));
        unsafePatterns.add(Pattern.compile("load\\s*\\("));
        unsafePatterns.add(Pattern.compile("loadWithNewGlobal\\s*\\("));
        unsafePatterns.add(Pattern.compile("exit\\s*\\("));
        unsafePatterns.add(Pattern.compile("quit\\s*\\("));
        unsafePatterns.add(Pattern.compile("eval\\s*\\("));

        // GraalJS 特有的不安全模式
        unsafePatterns.add(Pattern.compile("Polyglot\\."));
        unsafePatterns.add(Pattern.compile("Java\\.type\\s*\\("));
        unsafePatterns.add(Pattern.compile("Context\\."));
        unsafePatterns.add(Pattern.compile("Engine\\."));

        // 原型污染检测
        unsafePatterns.add(Pattern.compile("(?:Object|Array|String|Number|Boolean|Function|RegExp|Date)\\.prototype"));
        unsafePatterns.add(Pattern.compile("\\['constructor'\\]"));
        unsafePatterns.add(Pattern.compile("\\[\"constructor\"\\]"));
        unsafePatterns.add(Pattern.compile("\\['__proto__'\\]"));
        unsafePatterns.add(Pattern.compile("\\[\"__proto__\"\\]"));

        // 检测可能导致无限递归或循环的模式
        recursionPatterns.add(Pattern.compile("for\\s*\\([^\\)]*\\)\\s*\\{[^\\}]*for\\s*\\(")); // 嵌套循环
        recursionPatterns.add(Pattern.compile("while\\s*\\([^\\)]*\\)\\s*\\{[^\\}]*while\\s*\\(")); // 嵌套 while
        recursionPatterns.add(Pattern.compile("function\\s+[a-zA-Z0-9_$]+\\s*\\([^\\)]*\\)\\s*\\{[^\\}]*\\1\\s*\\(")); // 递归函数调用
    }

    @Override
    public boolean validate(String script) {
        if (StrUtil.isBlank(script)) {
            return true;
        }

        // 检查脚本长度
        if (script.length() > MAX_SCRIPT_LENGTH) {
            log.warn("脚本长度超过限制: {} > {}", script.length(), MAX_SCRIPT_LENGTH);
            return false;
        }

        // 使用超时机制进行验证
        final boolean[] result = {true};
        Thread validationThread = new Thread(() -> {
            // 检查不安全的关键字
            if (containsUnsafeKeywords(script)) {
                result[0] = false;
                return;
            }

            // 检查不安全的模式
            if (matchesUnsafePatterns(script)) {
                result[0] = false;
                return;
            }

            // 检查可能导致高资源消耗的构造
            if (containsHighResourcePatterns(script)) {
                log.warn("脚本包含可能导致高资源消耗的构造，需要注意");
                // 不直接拒绝，而是记录警告
            }

            // 分析脚本复杂度
            analyzeScriptComplexity(script);
        });

        validationThread.start();
        try {
            validationThread.join(VALIDATION_TIMEOUT);
            if (validationThread.isAlive()) {
                validationThread.interrupt();
                log.warn("脚本安全验证超时");
                return false;
            }
        } catch (InterruptedException e) {
            log.warn("脚本安全验证被中断");
            return false;
        }

        return result[0];
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * 检查脚本是否包含不安全的关键字
     *
     * @param script 脚本内容
     * @return 是否包含不安全的关键字
     */
    private boolean containsUnsafeKeywords(String script) {
        if (CollUtil.isEmpty(unsafeKeywords)) {
            return false;
        }

        for (String keyword : unsafeKeywords) {
            if (script.contains(keyword)) {
                log.warn("脚本包含不安全的关键字: {}", keyword);
                return true;
            }
        }
        return false;
    }

    /**
     * 检查脚本是否匹配不安全的模式
     *
     * @param script 脚本内容
     * @return 是否匹配不安全的模式
     */
    private boolean matchesUnsafePatterns(String script) {
        if (CollUtil.isEmpty(unsafePatterns)) {
            return false;
        }

        for (Pattern pattern : unsafePatterns) {
            Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                log.warn("脚本匹配到不安全的模式: {}", pattern.pattern());
                return true;
            }
        }
        return false;
    }

    /**
     * 检查脚本是否包含可能导致高资源消耗的模式
     *
     * @param script 脚本内容
     * @return 是否包含高资源消耗模式
     */
    private boolean containsHighResourcePatterns(String script) {
        if (CollUtil.isEmpty(highResourceKeywords)) {
            return false;
        }

        boolean result = false;
        for (String pattern : highResourceKeywords) {
            if (script.contains(pattern)) {
                log.warn("脚本包含高资源消耗模式: {}", pattern);
                result = true;
            }
        }

        // 还要检查递归或嵌套循环模式
        for (Pattern pattern : recursionPatterns) {
            Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                log.warn("脚本包含嵌套循环或递归调用: {}", pattern.pattern());
                result = true;
            }
        }

        return result;
    }

    /**
     * 分析脚本复杂度
     *
     * @param script 脚本内容
     */
    private void analyzeScriptComplexity(String script) {
        // 计算循环和条件语句的数量
        int forCount = countOccurrences(script, "for(");
        forCount += countOccurrences(script, "for (");

        int whileCount = countOccurrences(script, "while(");
        whileCount += countOccurrences(script, "while (");

        int doWhileCount = countOccurrences(script, "do{");
        doWhileCount += countOccurrences(script, "do {");

        int funcCount = countOccurrences(script, "function");

        // 记录复杂度评估
        if (forCount + whileCount + doWhileCount > 10) {
            log.warn("脚本循环结构过多: for={}, while={}, do-while={}", forCount, whileCount, doWhileCount);
        }

        if (funcCount > 20) {
            log.warn("脚本函数定义过多: {}", funcCount);
        }
    }

    /**
     * 计算字符串出现次数
     *
     * @param source    源字符串
     * @param substring 子字符串
     * @return 出现次数
     */
    private int countOccurrences(String source, String substring) {
        int count = 0;
        int index = 0;
        while ((index = source.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}