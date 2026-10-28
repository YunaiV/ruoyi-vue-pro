package cn.iocoder.yudao.module.iot.core.script.rhino;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.script.IotScriptException;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptResult;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import java.util.LinkedHashMap;
import java.util.Map;

// DONE @AI：字段注释；
/**
 * 单个 JS worker 线程独占的 Rhino 运行时
 * <ul>
 *     <li>sealedStandard：initSafeStandardObjects(null, true) 创建的标准对象，不含 Packages / java / JavaAdapter / getClass，已封存；</li>
 *     <li>userScripts：按 productId:hash 的编译缓存，只在本线程使用；</li>
 *     <li>每次调用新建一个可写作用域，用完即丢，调用之间不保留任何状态。</li>
 * </ul>
 * 不跨线程共享 Script，也不跨线程共享作用域
 *
 * @author 芋道源码
 */
final class IotScriptRuntime {

    /**
     * 脚本输出（序列化后的 JSON）的最大字符数，超过按输出不合法处理
     */
    static final int MAX_RESULT_CHARS = 65536;
    /**
     * 编译缓存最多保留的脚本数：超过时淘汰最久未使用的
     */
    private static final int MAX_COMPILED_SCRIPTS = 64;

    /**
     * IoT 专用的 Rhino ContextFactory：每次编译、调用都由它进入 Context
     */
    private final IotScriptContextFactory factory;
    /**
     * 创建本运行时的 JS 线程：只能在该线程上使用
     */
    private final Thread owner = Thread.currentThread();
    /**
     * 封存的标准对象：每次调用的作用域都以它为原型（prototype），作用域自身没有父作用域
     */
    private final ScriptableObject sealedStandard;
    /**
     * 封存的标准对象里的 JSON 对象：宿主只用它做 parse/stringify，用户脚本里的同名变量影响不到
     */
    private final Scriptable json;
    /**
     * 封存的 JSON.parse：把平台构造的入参 JSON 转为脚本里的 ctx 对象
     */
    private final Function jsonParse;
    /**
     * 封存的 JSON.stringify：把脚本的返回值序列化为结果 JSON
     */
    private final Function jsonStringify;
    /**
     * 编译缓存：key = productId:sha256（调试时为 debug:sha256）；只在本线程访问
     */
    private final Map<String, Script> userScripts = new LinkedHashMap<String, Script>(16, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Script> eldest) {
            return size() > MAX_COMPILED_SCRIPTS;
        }

    };

    IotScriptRuntime(IotScriptContextFactory factory) {
        this.factory = factory;
        Context cx = factory.enterContext();
        try {
            this.sealedStandard = cx.initSafeStandardObjects(null, true); // 第二个参数 true = 封存
            this.json = (Scriptable) ScriptableObject.getProperty(sealedStandard, "JSON");
            this.jsonParse = (Function) ScriptableObject.getProperty(json, "parse");
            this.jsonStringify = (Function) ScriptableObject.getProperty(json, "stringify");
        } finally {
            Context.exit();
        }
    }

    /**
     * 只编译、不执行任何用户代码：保存前的语法检查（biz）和首次调用前的编译（gateway）都用它
     *
     * @throws org.mozilla.javascript.EvaluatorException 语法错误，带行号
     */
    static Script compile(Context cx, String name, String source) {
        return cx.compileString(source, name, 1, null);
    }

    /**
     * 只做语法检查：不执行任何用户代码，也不放入编译缓存
     *
     * @param content 脚本内容
     * @throws IotScriptException 语法错误
     */
    void compileOnly(String content) throws IotScriptException {
        checkOwner();
        Context cx = factory.enterContext();
        try {
            compile(cx, "product", content);
        } catch (RhinoException e) {
            throw new IotScriptException(IotScriptResult.Status.ERROR,
                    StrUtil.maxLength(e.details(), 200), e.lineNumber());
        } finally {
            Context.exit();
        }
    }

    /**
     * 执行一次
     *
     * @param source   脚本
     * @param function "decode" 或 "encode"
     * @param ctxJson  平台构造的 JSON 字符串（不含 Java 对象、byte[]、Map、List 或任何秘密；字节是数字数组）
     * @param budget   执行器传入的本次预算：下面的第 2~5 步都受它约束
     * @return 结果 JSON 字符串；null 表示脚本返回了 null/undefined，或没有定义该函数
     */
    String invoke(IotScriptSource source, String function, String ctxJson, IotScriptBudget budget) throws IotScriptException {
        checkOwner();
        Context cx = factory.enterContext();
        cx.putThreadLocal(IotScriptBudget.class, budget); // 先挂预算，之后的一切解释执行都计数
        try {
            // 1. 编译缓存：首次使用时在本线程编译（编译本身不经过指令观察器，只受 JS 任务的时间兜底约束；源码不超过 32KB）
            Script user = userScripts.get(source.getKey());
            if (user == null) {
                user = compile(cx, "product-" + source.getProductId(), source.getContent());
                userScripts.put(source.getKey(), user);
            }
            // 2. 新的可写作用域：原型是封存的标准对象、没有父作用域，脚本能定义自己的变量，但改不了 Array.prototype 等标准对象
            Scriptable scope = cx.newObject(sealedStandard);
            scope.setPrototype(sealedStandard);
            scope.setParentScope(null);
            // 3. 用户源码顶层在这个作用域中执行（每次都执行），定义 decode / encode
            user.exec(cx, scope);
            Object fn = ScriptableObject.getProperty(scope, function);
            if (!(fn instanceof Function)) {
                return null; // 没有定义这个方向的函数：等同于返回 null
            }
            // 4. 入参原生化：JSON 字符串 → JS 原生对象（字节是原生数字数组）；不传任何 Java 对象
            Object ctx = jsonParse.call(cx, scope, json, new Object[]{ctxJson});
            Object result = ((Function) fn).call(cx, scope, scope, new Object[]{ctx});
            if (result == null || result instanceof Undefined) {
                return null;
            }
            // 5. 结果序列化：result 的 toJSON / getter 也在这里执行，同样受预算约束
            Object out = jsonStringify.call(cx, scope, json, new Object[]{result});
            if (!(out instanceof String)) { // 函数等不能序列化的值得到 undefined：按输出不合法处理
                throw new IotScriptException(IotScriptResult.Status.OUTPUT_INVALID, "脚本返回值不能序列化为 JSON", -1);
            }
            String text = (String) out;
            if (text.length() > MAX_RESULT_CHARS) {
                throw new IotScriptException(IotScriptResult.Status.OUTPUT_INVALID, "脚本输出超过 " + MAX_RESULT_CHARS + " 字符", -1);
            }
            return text;
        } catch (RhinoException e) { // 语法错误、运行错误、throw、封存对象被修改、超出栈深
            throw new IotScriptException(IotScriptResult.Status.ERROR,
                    StrUtil.maxLength(e.details(), 200), e.lineNumber());
        } finally {
            cx.removeThreadLocal(IotScriptBudget.class); // 清理预算
            Context.exit(); // 退出 Context；BudgetExceededError、OOM 等 Error 照常向上抛
        }
    }

    private void checkOwner() {
        if (Thread.currentThread() != owner) {
            throw new IllegalStateException("IotScriptRuntime 只能在创建它的 JS 线程上使用");
        }
    }

}
