package cn.iocoder.yudao.module.deepay.agent;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 商品信息提取 Agent。
 *
 * <p>职责：根据用户的 prompt 生成结构化商品信息，填充
 * {@link Context#title}、{@link Context#description}、{@link Context#price}。</p>
 *
 * <p>MVP 策略：
 * <ul>
 *   <li>title —— 直接使用用户输入（上限 100 字符）。</li>
 *   <li>description —— 在 prompt 基础上拼接固定风格描述词，体现品质感。</li>
 *   <li>price —— 使用默认起售价 299 欧元，后续可接入 AI 定价模型替换。</li>
 * </ul>
 * </p>
 */
@Component
public class ProductInfoAgent implements Agent {

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("299.00");
    private static final int TITLE_MAX_LEN = 100;

    @Override
    public Context run(Context ctx) {
        String prompt = StringUtils.hasText(ctx.prompt) ? ctx.prompt.trim() : "高端时装";

        // 标题：取用户 prompt（超长截断）
        ctx.title = prompt.length() > TITLE_MAX_LEN ? prompt.substring(0, TITLE_MAX_LEN) : prompt;

        // 描述：在 prompt 基础上补充品质修饰语
        ctx.description = prompt + "，精选顶级面料，匠心工艺，简约而不简单。";

        // 价格：MVP 使用固定起售价，后续可由 AI 动态定价替换
        ctx.price = DEFAULT_PRICE;

        return ctx;
    }

}
