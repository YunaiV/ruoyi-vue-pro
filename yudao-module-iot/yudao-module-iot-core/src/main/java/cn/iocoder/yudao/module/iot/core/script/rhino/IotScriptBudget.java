package cn.iocoder.yudao.module.iot.core.script.rhino;

/**
 * 一次脚本调用的执行预算：用户源码顶层、ctx 原生化、函数调用、结果序列化共用同一个预算对象
 * <p>
 * 只在本次调用的 JS 线程上使用，不需要同步
 *
 * @author 芋道源码
 */
final class IotScriptBudget {

    /**
     * 截止时间（System.nanoTime）= min(开始执行 + execBudget, 调用方传入的截止时间)
     */
    private final long deadlineNanos;
    /**
     * 指令上限
     */
    private final long maxInstructions;
    /**
     * 已执行的指令数
     */
    private long usedInstructions;

    IotScriptBudget(long deadlineNanos, long maxInstructions) {
        this.deadlineNanos = deadlineNanos;
        this.maxInstructions = maxInstructions;
    }

    void consume(int count) {
        usedInstructions += count;
        if (usedInstructions >= maxInstructions) {
            throw new BudgetExceededError("超过指令预算");
        }
        if (System.nanoTime() >= deadlineNanos) {
            throw new BudgetExceededError("超过执行时间预算");
        }
    }

    /**
     * 观察器抛出的宿主 Error：Rhino 原样向上抛，脚本的 catch/finally 拦不住
     * <p>
     * 一旦超限，之后每个检查点都会再抛，所以 finally 中的死循环也会被中断
     */
    static final class BudgetExceededError extends Error {

        BudgetExceededError(String message) {
            super(message, null, false, false); // 不收集栈：开销小
        }

    }

}
