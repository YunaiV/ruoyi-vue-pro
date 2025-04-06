package cn.iocoder.yudao.module.iot.script.example;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.script.context.DefaultScriptContext;
import cn.iocoder.yudao.module.iot.script.context.ScriptContext;
import cn.iocoder.yudao.module.iot.script.service.ScriptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * GraalJS 脚本引擎示例
 * <p>
 * 展示了如何使用 GraalJS 脚本引擎的各种功能
 */
@Slf4j
@Component
public class GraalJsExample {

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
     * 执行现代 JavaScript 语法（ES6+）
     *
     * @return 执行结果
     */
    public Object executeModernJavaScript() {
        // 使用现代 JavaScript 语法
        String script = "// 使用箭头函数\n" +
                "const add = (a, b) => a + b;\n" +
                "\n" +
                "// 使用解构赋值\n" +
                "const {c, d} = params;\n" +
                "\n" +
                "// 使用模板字符串\n" +
                "const result = `计算结果: ${add(c, d)}`;\n" +
                "\n" +
                "// 使用可选链操作符\n" +
                "const value = params?.e?.value ?? 'default';\n" +
                "\n" +
                "// 返回一个对象\n" +
                "({\n" +
                "    sum: add(c, d),\n" +
                "    message: result,\n" +
                "    defaultValue: value\n" +
                "})";

        // 创建参数
        Map<String, Object> params = MapUtil.newHashMap();
        params.put("params", MapUtil.builder()
                .put("c", 30)
                .put("d", 40)
                .build());

        // 执行脚本
        return scriptService.executeJavaScript(script, params);
    }

    /**
     * 执行带错误处理的脚本
     *
     * @return 执行结果
     */
    public Object executeWithErrorHandling() {
        // 包含错误处理的脚本
        String script = "try {\n" +
                "    // 故意制造错误\n" +
                "    if (!nonExistentVar) {\n" +
                "        throw new Error('手动抛出的错误');\n" +
                "    }\n" +
                "} catch (error) {\n" +
                "    console.error('捕获到错误: ' + error.message);\n" +
                "    return { success: false, error: error.message };\n" +
                "}\n" +
                "\n" +
                "return { success: true, data: 'No error' };";

        // 执行脚本
        return scriptService.executeJavaScript(script, MapUtil.newHashMap());
    }

    /**
     * 演示超时控制
     *
     * @return 执行结果
     */
    public Object executeWithTimeout() {
        // 这个脚本会导致无限循环
        String script = "// 无限循环\n" +
                "var counter = 0;\n" +
                "while(true) {\n" +
                "    counter++;\n" +
                "    if (counter % 1000000 === 0) {\n" +
                "        console.log('Still running: ' + counter);\n" +
                "    }\n" +
                "}\n" +
                "return counter;";

        // 使用 CompletableFuture 和超时控制
        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            try {
                return scriptService.executeJavaScript(script, MapUtil.newHashMap());
            } catch (Exception e) {
                log.error("脚本执行失败: {}", e.getMessage());
                return "执行失败: " + e.getMessage();
            }
        });

        try {
            // 等待结果，最多 10 秒
            return future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            return "执行异常: " + e.getMessage();
        } catch (TimeoutException e) {
            future.cancel(true);
            return "执行超时，已强制终止";
        }
    }

    /**
     * 演示 JSON 处理
     *
     * @return 执行结果
     */
    public Object executeJsonProcessing() {
        // JSON 处理示例
        String script = "// 解析传入的 JSON\n" +
                "var data = JSON.parse(jsonString);\n" +
                "\n" +
                "// 处理 JSON 数据\n" +
                "var result = {\n" +
                "    name: data.name.toUpperCase(),\n" +
                "    age: data.age + 1,\n" +
                "    address: data.address || 'Unknown',\n" +
                "    tags: [...data.tags, 'processed'],\n" +
                "    timestamp: Date.now()\n" +
                "};\n" +
                "\n" +
                "// 转换回 JSON 字符串\n" +
                "JSON.stringify(result);";

        // 创建上下文
        ScriptContext context = new DefaultScriptContext();
        context.setParameter("jsonString",
                "{\"name\":\"test user\",\"age\":25,\"tags\":[\"tag1\",\"tag2\"]}");

        // 执行脚本
        return scriptService.executeJavaScript(script, context);
    }

    /**
     * 演示数据转换
     *
     * @return 执行结果
     */
    public Object executeDataConversion() {
        // 数据转换和处理示例
        String script = "// 使用 utils 工具类进行数据转换\n" +
                "var stringValue = utils.isEmpty(input) ? \"默认值\" : input;\n" +
                "var numberValue = utils.convert(stringValue, \"number\");\n" +
                "\n" +
                "// 创建一个复杂数据结构\n" +
                "var result = {\n" +
                "    original: input,\n" +
                "    stringValue: stringValue,\n" +
                "    numberValue: numberValue,\n" +
                "    booleanValue: Boolean(numberValue),\n" +
                "    isValid: utils.isNotEmpty(input)\n" +
                "};\n" +
                "\n" +
                "// 记录处理结果\n" +
                "log.info(\"处理结果: \" + utils.toJson(result));\n" +
                "\n" +
                "return result;";

        // 创建参数
        Map<String, Object> params = MapUtil.newHashMap();
        params.put("input", "42");

        // 执行脚本
        return scriptService.executeJavaScript(script, params);
    }
}