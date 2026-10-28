package cn.iocoder.yudao.module.iot.core.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * IoT 脚本专用的 Rhino ContextFactory：不使用 ContextFactory.getGlobal()，不影响进程内其它 Rhino 使用方
 *
 * @author 芋道源码
 */
final class IotScriptContextFactory extends ContextFactory {

    /**
     * 每执行这么多条解释器指令，回调一次 observeInstructionCount
     */
    static final int INSTRUCTION_THRESHOLD = 1000;
    /**
     * 解释器最大栈深（递归深度）
     */
    static final int MAX_STACK_DEPTH = 64;

    @Override
    protected Context makeContext() {
        Context cx = super.makeContext();
        cx.setOptimizationLevel(-1); // 解释模式：指令观察器只在解释模式下生效
        cx.setLanguageVersion(Context.VERSION_1_8);
        cx.setInstructionObserverThreshold(INSTRUCTION_THRESHOLD);
        cx.setMaximumInterpreterStackDepth(MAX_STACK_DEPTH); // 超出 → EvaluatorException「Exceeded maximum stack depth」
        cx.setClassShutter(className -> false); // 拒绝任何 Java 类
        cx.setGeneratingDebug(false);
        return cx;
    }

    @Override
    protected void observeInstructionCount(Context cx, int instructionCount) {
        IotScriptBudget budget = (IotScriptBudget) cx.getThreadLocal(IotScriptBudget.class);
        if (budget == null) {
            throw new IotScriptBudget.BudgetExceededError("缺少执行预算"); // fail-closed：没有预算的 Context 不允许继续执行
        }
        budget.consume(instructionCount); // 超限时抛 BudgetExceededError
    }

}
