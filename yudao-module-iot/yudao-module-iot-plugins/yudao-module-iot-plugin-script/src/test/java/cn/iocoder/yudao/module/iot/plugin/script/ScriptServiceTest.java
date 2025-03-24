package cn.iocoder.yudao.module.iot.plugin.script;

import cn.iocoder.yudao.module.iot.plugin.script.context.PluginScriptContext;
import cn.iocoder.yudao.module.iot.plugin.script.engine.ScriptEngineFactory;
import cn.iocoder.yudao.module.iot.plugin.script.service.ScriptService;
import cn.iocoder.yudao.module.iot.plugin.script.service.ScriptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 脚本服务单元测试
 */
class ScriptServiceTest {

    private ScriptService scriptService;

    @BeforeEach
    void setUp() {
        ScriptEngineFactory engineFactory = new ScriptEngineFactory();
        ScriptServiceImpl service = new ScriptServiceImpl();

        // 使用反射设置engineFactory
        try {
            java.lang.reflect.Field field = ScriptServiceImpl.class.getDeclaredField("engineFactory");
            field.setAccessible(true);
            field.set(service, engineFactory);
        } catch (Exception e) {
            throw new RuntimeException("设置测试依赖失败", e);
        }

        service.init(); // 手动调用初始化方法
        this.scriptService = service;
    }

    @Test
    void testExecuteSimpleScript() {
        // 准备
        String script = "var result = a + b; result;";
        Map<String, Object> params = new HashMap<>();
        params.put("a", 10);
        params.put("b", 20);

        // 执行
        Object result = scriptService.executeJavaScript(script, params);

        // 验证 - 使用delta比较，允许浮点数和整数比较
        assertEquals(30.0, ((Number) result).doubleValue(), 0.001);
    }

    @Test
    void testExecuteObjectResult() {
        // 准备
        String script = "var obj = { name: 'test', value: 123 }; obj;";

        // 执行
        Object result = scriptService.executeJavaScript(script, new HashMap<>());

        // 验证
        assertNotNull(result);
        assertTrue(result instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals("test", map.get("name"));

        // 对于数值，先转换为double再比较
        assertEquals(123.0, ((Number) map.get("value")).doubleValue(), 0.001);
    }

    @Test
    void testExecuteWithContext() {
        // 准备
        String script = "var message = 'Hello, ' + name + '!'; message;";
        PluginScriptContext context = new PluginScriptContext();
        context.setParameter("name", "World");

        // 执行
        Object result = scriptService.executeJavaScript(script, context);

        // 验证
        assertEquals("Hello, World!", result);
    }

    @Test
    void testScriptWithFunction() {
        // 准备
        String script = "function add(x, y) { return x + y; } add(a, b);";
        Map<String, Object> params = new HashMap<>();
        params.put("a", 15);
        params.put("b", 25);

        // 执行
        Object result = scriptService.executeJavaScript(script, params);

        // 验证 - 使用delta比较，允许浮点数和整数比较
        assertEquals(40.0, ((Number) result).doubleValue(), 0.001);
    }

    @Test
    void testExecuteInvalidScript() {
        // 准备
        String script = "invalid syntax";

        // 执行和验证
        assertThrows(RuntimeException.class, () -> {
            scriptService.executeJavaScript(script, new HashMap<>());
        });
    }

    @Test
    void testScriptTimeout() {
        // 准备 - 一个无限循环的脚本
        String script = "while(true) { }";

        // 执行和验证
        assertThrows(RuntimeException.class, () -> {
            scriptService.executeJavaScript(script, new HashMap<>());
        });
    }
}