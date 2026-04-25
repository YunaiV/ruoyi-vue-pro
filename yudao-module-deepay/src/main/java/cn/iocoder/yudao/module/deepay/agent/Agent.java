package cn.iocoder.yudao.module.deepay.agent;

/**
 * Agent 统一接口 —— 流程中每一步的处理单元。
 *
 * <p>每个 Agent 接收 {@link Context}，完成自身职责后返回（可能已修改的）同一个 Context，
 * 保证整条流水线只通过单一数据载体传递状态，避免 Agent 之间直接依赖。</p>
 */
public interface Agent {

    /**
     * 执行 Agent 逻辑。
     *
     * @param ctx 全流程共享的数据载体，不得为 null
     * @return 处理后的 ctx（通常是同一个对象，字段已被写入）
     */
    Context run(Context ctx);

}
