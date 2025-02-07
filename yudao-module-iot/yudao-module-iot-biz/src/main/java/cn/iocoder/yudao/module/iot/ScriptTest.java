package cn.iocoder.yudao.module.iot;

import cn.hutool.script.ScriptUtil;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * TODO 芋艿：测试脚本的接入
 */
public class ScriptTest {

    public static void main2(String[] args) {
        // 创建一个 Groovy 脚本引擎
        ScriptEngine engine = ScriptUtil.createGroovyEngine();

        // 创建绑定参数
        Bindings bindings = engine.createBindings();
        bindings.put("name", "Alice");
        bindings.put("age", 30);

        // 定义一个稍微复杂的 Groovy 脚本
        String script = "def greeting = 'Hello, ' + name + '!';\n" +
                "def ageInFiveYears = age + 5;\n" +
                "def message = greeting + ' In five years, you will be ' + ageInFiveYears + ' years old.';\n" +
                "return message.toUpperCase();\n";

        try {
            // 执行脚本并获取结果
            Object result = engine.eval(script, bindings);
            System.out.println(result); // 输出: HELLO, ALICE! IN FIVE YEARS, YOU WILL BE 35 YEARS OLD.
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 创建一个 JavaScript 脚本引擎
        ScriptEngine jsEngine = ScriptUtil.createJsEngine();

        // 创建绑定参数
        Bindings jsBindings = jsEngine.createBindings();
        jsBindings.put("name", "Bob");
        jsBindings.put("age", 25);

        // 定义一个简单的 JavaScript 脚本
        String jsScript = "var greeting = 'Hello, ' + name + '!';\n" +
                "var ageInTenYears = age + 10;\n" +
                "var message = greeting + ' In ten years, you will be ' + ageInTenYears + ' years old.';\n" +
                "message.toUpperCase();\n";

        try {
            // 执行脚本并获取结果
            Object jsResult = jsEngine.eval(jsScript, jsBindings);
            System.out.println(jsResult); // 输出: HELLO, BOB! IN TEN YEARS, YOU WILL BE 35 YEARS OLD.
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

}
