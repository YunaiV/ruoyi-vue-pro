package cn.iocoder.yudao.module.deepay.orchestrator;

import cn.iocoder.yudao.module.deepay.agent.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 链路流程编排器（Orchestrator）。
 *
 * <p>按顺序串行调用各 Agent，实现「一句话 → 设计 → 选择 → 生成链码 → 初始化库存 → ima同步 → 生成支付 → 返回」完整闭环。</p>
 *
 * <ul>
 *   <li>所有步骤在同一请求线程内同步执行，不引入任何异步机制。</li>
 *   <li>{@link ChainAgent}、{@link ImaAgent}、{@link InventoryInitAgent} 是 Spring Bean（需要数据库 / 外部服务访问），
 *       通过 {@link Resource} 注入；其余无依赖的 Agent 直接 {@code new} 创建，保持轻量。</li>
 * </ul>
 */
@Service
public class ChainOrchestrator {

    /** DesignAgent 依赖 FluxService，必须由 Spring 管理 */
    @Resource
    private DesignAgent designAgent;

    /** ProductInfoAgent 依赖 Spring 管理，提取商品标题/描述/价格 */
    @Resource
    private ProductInfoAgent productInfoAgent;

    /** ChainAgent 依赖 Mapper，必须由 Spring 管理 */
    @Resource
    private ChainAgent chainAgent;

    /** InventoryInitAgent 依赖 InventoryService，必须由 Spring 管理 */
    @Resource
    private InventoryInitAgent inventoryInitAgent;

    /** ImaAgent 依赖 Mapper 和可选 ImaService，必须由 Spring 管理 */
    @Resource
    private ImaAgent imaAgent;

    /**
     * 执行完整商品生成流水线。
     *
     * @param prompt 用户输入的一句话需求
     * @return 填充完毕的 {@link Context}，包含 chainCode、selectedImage、iban 等字段
     */
    public Context run(String prompt) {
        Context ctx = new Context();
        ctx.keyword = prompt;
        ctx.prompt  = prompt; // backward compat

        // 1. 生成设计图（使用 keyword）
        ctx = designAgent.run(ctx);
        // 2. 选图（取第一张）
        ctx = new DecisionAgent().run(ctx);
        // 3. 提取商品信息（标题、描述、价格）—— 必须在 ChainAgent 之前，以便落库
        ctx = productInfoAgent.run(ctx);
        // 4. 生成链码并落库（必须在库存初始化之前完成）
        ctx = chainAgent.run(ctx);
        // 5. 初始化库存（默认库存 10 件）
        ctx = inventoryInitAgent.run(ctx);
        // 6. ima 同步（副本，失败不影响主流程）
        ctx = imaAgent.run(ctx);
        // 7. 生成收款 IBAN（mock）
        ctx = new FinanceAgent().run(ctx);

        return ctx;
    }

}
