package cn.iocoder.yudao.module.ai.service.image;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AI 服装设计意图链
 *
 * <p>对应 Python 参考代码中的多意图检测：一句话可同时触发多个意图，
 * 例如"帮我设计5款甜酷风红色连衣裙然后转3D旋转90度"会解析为：
 * <pre>
 * BATCH_GENERATE(5款甜酷风红色连衣裙) → GENERATE_3D → ROTATE(90°)
 * </pre>
 * 所有步骤自动按序执行，无需用户手动触发每一步。</p>
 *
 * @author deepay
 */
@Getter
@Builder
public final class AiFashionIntentChain {

    /** 链路中每一步的解析结果（按执行顺序排列） */
    private final List<AiFashionIntentParseResult> steps;

    /** 人类可读的链路描述，如"为您执行：生成5款红色裙子 → 转3D → 旋转90°" */
    private final String chainDescription;

    /** 是否自动链式执行（无需用户确认每一步，默认 true） */
    private final boolean autoChain;

    /** 首步 prompt（来自第一个 step 的 basePrompt）*/
    public String getFirstPrompt() {
        return steps.isEmpty() ? "" : steps.get(0).getBasePrompt();
    }

    /** 步骤数量 */
    public int size() {
        return steps == null ? 0 : steps.size();
    }

}
