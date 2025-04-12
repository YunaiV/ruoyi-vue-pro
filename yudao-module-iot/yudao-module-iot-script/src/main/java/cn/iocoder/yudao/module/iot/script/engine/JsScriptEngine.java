package cn.iocoder.yudao.module.iot.script.engine;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.script.context.ScriptContext;
import cn.iocoder.yudao.module.iot.script.sandbox.ScriptSandbox;
import cn.iocoder.yudao.module.iot.script.util.ScriptUtils;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.*;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JavaScript 脚本引擎实现，基于 GraalJS Context API
 */
@Slf4j
public class JsScriptEngine extends AbstractScriptEngine implements AutoCloseable {

    /**
     * JavaScript 引擎类型
     */
    public static final String TYPE = "js";

    /**
     * 脚本语言类型
     */
    private static final String LANGUAGE_ID = "js";

    /**
     * GraalJS 上下文
     */
    private final Context context;

    /**
     * 脚本源代码缓存
     */
    private final Map<String, Source> sourceCache = new ConcurrentHashMap<>();

    /**
     * 脚本缓存的最大数量
     */
    private static final int MAX_CACHE_SIZE = 1000;

    /**
     * 构造函数
     *
     * @param sandbox JavaScript 沙箱
     */
    public JsScriptEngine(ScriptSandbox sandbox) {
        super(sandbox);

        // 创建安全的主机访问配置
        HostAccess hostAccess = HostAccess.newBuilder()
                .allowPublicAccess(true) // 允许访问公共方法和字段
                .allowArrayAccess(true) // 允许数组访问
                .allowListAccess(true) // 允许 List 访问
                .allowMapAccess(true) // 允许 Map 访问
                .build();

        // 创建隔离的临时目录路径
        // TODO @haohao：貌似没用到？
        Path tempDirectory = Path.of(System.getProperty("java.io.tmpdir"), "graaljs-" + IdUtil.fastSimpleUUID());

        // 初始化 GraalJS 上下文
        this.context = Context.newBuilder(LANGUAGE_ID)
                .allowHostAccess(hostAccess) // 使用安全的主机访问配置
                .allowHostClassLookup(className -> false) // 禁止查找 Java 类
                .allowIO(false) // 禁止文件 IO
                .allowNativeAccess(false) // 禁止本地访问
                .allowCreateThread(false) // 禁止创建线程
                .allowEnvironmentAccess(org.graalvm.polyglot.EnvironmentAccess.NONE) // 禁止环境变量访问
                .allowExperimentalOptions(false) // 禁止实验性选项
                .option("js.ecmascript-version", "2021") // 使用最新的 ECMAScript 标准
                .option("js.foreign-object-prototype", "false") // 禁用外部对象原型
                .option("js.nashorn-compat", "false") // 关闭 Nashorn 兼容模式以获得更好性能
                .build();
    }

    @Override
    protected Object doExecute(String script, ScriptContext context) throws Exception {
        if (StrUtil.isBlank(script)) {
            return null;
        }

        try {
            // 绑定上下文变量
            bindContextVariables(context);

            // 从缓存获取或创建脚本源
            Source source = getOrCreateSource(script);

            // 执行脚本并捕获结果，添加超时控制
            // TODO @haohao：通过线程池 + future 会好点？
            Value result;
            Thread executionThread = Thread.currentThread();
            Thread watchdogThread = new Thread(() -> {
                try {
                    // 等待 5 秒
                    TimeUnit.SECONDS.sleep(5);
                    // 如果执行线程还在运行，中断它
                    if (executionThread.isAlive()) {
                        log.warn("脚本执行超时，强制中断");
                        executionThread.interrupt();
                    }
                } catch (InterruptedException ignored) {
                    // 忽略中断
                }
            });

            watchdogThread.setDaemon(true);
            watchdogThread.start();

            try {
                result = this.context.eval(source);
            } finally {
                watchdogThread.interrupt(); // 确保看门狗线程停止
            }

            // 转换结果为 Java 对象
            return convertResultToJava(result);
        } catch (PolyglotException e) {
            handleScriptException(e, script);
            throw e;
        }
    }

    /**
     * 绑定上下文变量
     *
     * @param context 脚本上下文
     */
    private void bindContextVariables(ScriptContext context) {
        Value bindings = this.context.getBindings(LANGUAGE_ID);

        // 添加上下文参数
        if (MapUtil.isNotEmpty(context.getParameters())) {
            context.getParameters().forEach(bindings::putMember);
        }

        // 添加上下文函数
        if (MapUtil.isNotEmpty(context.getFunctions())) {
            context.getFunctions().forEach(bindings::putMember);
        }

        // 添加工具类
        bindings.putMember("utils", ScriptUtils.getInstance());

        // 添加日志对象
        bindings.putMember("log", log);

        // 添加控制台输出（限制并重定向到日志）
        AtomicReference<StringBuilder> consoleBuffer = new AtomicReference<>(new StringBuilder());

        Value console = this.context.eval(LANGUAGE_ID, "({\n" +
                "  log: function(msg) { _consoleLog(msg, 'INFO'); },\n" +
                "  info: function(msg) { _consoleLog(msg, 'INFO'); },\n" +
                "  warn: function(msg) { _consoleLog(msg, 'WARN'); },\n" +
                "  error: function(msg) { _consoleLog(msg, 'ERROR'); }\n" +
                "})");

        bindings.putMember("console", console);

        bindings.putMember("_consoleLog", (java.util.function.BiConsumer<String, String>) (message, level) -> {
            String formattedMsg = String.valueOf(message);
            switch (level) {
                case "INFO":
                    log.info("Script console: {}", formattedMsg);
                    break;
                case "WARN":
                    log.warn("Script console: {}", formattedMsg);
                    break;
                case "ERROR":
                    log.error("Script console: {}", formattedMsg);
                    break;
                default:
                    log.info("Script console: {}", formattedMsg);
            }

            // 将输出添加到缓冲区
            StringBuilder buffer = consoleBuffer.get();
            if (buffer.length() > 10000) {
                buffer = new StringBuilder();
                consoleBuffer.set(buffer);
            }
            buffer.append(formattedMsg).append("\n");
        });
    }

    /**
     * 从缓存中获取或创建脚本源
     *
     * @param script 脚本内容
     * @return 脚本源
     */
    private Source getOrCreateSource(String script) {
        // 如果缓存太大，清理部分缓存
        if (sourceCache.size() > MAX_CACHE_SIZE) {
            int itemsToRemove = (int) (MAX_CACHE_SIZE * 0.2); // 清理 20% 的缓存
            sourceCache.keySet().stream()
                    .limit(itemsToRemove)
                    .toList()
                    .forEach(sourceCache::remove);
        }

        // 使用脚本的哈希码作为缓存键
        String cacheKey = String.valueOf(script.hashCode());

        return sourceCache.computeIfAbsent(cacheKey, key -> {
            try {
                return Source.newBuilder(LANGUAGE_ID, script, "script-" + key + ".js").cached(true).build();
            } catch (Exception e) {
                log.error("创建脚本源失败: {}", e.getMessage(), e);
                throw new RuntimeException("创建脚本源失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 将 GraalJS 结果转换为 Java 对象
     *
     * @param result GraalJS 执行结果
     * @return Java 对象
     */
    private Object convertResultToJava(Value result) {
        if (result == null || result.isNull()) {
            return null;
        }

        if (result.isString()) {
            return result.asString();
        }

        if (result.isNumber()) {
            if (result.fitsInInt()) {
                return result.asInt();
            }
            if (result.fitsInLong()) {
                return result.asLong();
            }
            if (result.fitsInFloat()) {
                return result.asFloat();
            }
            if (result.fitsInDouble()) {
                return result.asDouble();
            }
        }

        if (result.isBoolean()) {
            return result.asBoolean();
        }

        if (result.hasArrayElements()) {
            int size = (int) result.getArraySize();
            Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = convertResultToJava(result.getArrayElement(i));
            }
            return array;
        }

        if (result.hasMembers()) {
            Map<String, Object> map = MapUtil.newHashMap();
            for (String key : result.getMemberKeys()) {
                map.put(key, convertResultToJava(result.getMember(key)));
            }
            return map;
        }

        if (result.isHostObject()) {
            return result.asHostObject();
        }

        // 默认情况下尝试转换为字符串
        return result.toString();
    }

    /**
     * 处理脚本执行异常
     *
     * @param e      多语言异常
     * @param script 原始脚本
     */
    private void handleScriptException(PolyglotException e, String script) {
        if (e.isCancelled()) {
            log.error("脚本执行被取消，可能超出资源限制");
        } else if (e.isHostException()) {
            Throwable hostException = e.asHostException();
            log.error("脚本执行时发生 Java 异常: {}", hostException.getMessage(), hostException);
        } else if (e.isGuestException()) {
            if (e.getSourceLocation() != null) {
                log.error("脚本执行错误: {} 位于行 {}，列 {}",
                        e.getMessage(),
                        e.getSourceLocation().getStartLine(),
                        e.getSourceLocation().getStartColumn());

                // 尝试显示错误位置上下文
                try {
                    String[] lines = script.split("\n");
                    int lineNumber = e.getSourceLocation().getStartLine();
                    if (lineNumber > 0 && lineNumber <= lines.length) {
                        int contextStart = Math.max(1, lineNumber - 2);
                        int contextEnd = Math.min(lines.length, lineNumber + 2);

                        StringBuilder context = new StringBuilder();
                        for (int i = contextStart; i <= contextEnd; i++) {
                            if (i == lineNumber) {
                                context.append("> "); // 标记错误行
                            } else {
                                context.append("  ");
                            }
                            context.append(i).append(": ").append(lines[i - 1]).append("\n");
                        }
                        log.error("脚本上下文:\n{}", context);
                    }
                } catch (Exception ignored) {
                    // 忽略上下文显示失败
                }
            } else {
                log.error("脚本执行错误: {}", e.getMessage());
            }
        } else {
            log.error("脚本执行时发生未知错误: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void close() {
        try {
            // 清除脚本缓存
            sourceCache.clear();

            // 关闭 GraalJS 上下文，释放资源
            context.close(true);
        } catch (Exception e) {
            log.warn("关闭 GraalJS 引擎时发生错误: {}", e.getMessage());
        }
    }
}