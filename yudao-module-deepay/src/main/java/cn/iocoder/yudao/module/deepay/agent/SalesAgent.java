package cn.iocoder.yudao.module.deepay.agent;

import java.math.BigDecimal;

/**
 * SalesAgent —— 商品销售信息生成模块。
 *
 * <p>负责为商品填充标题、描述、价格和购买链接。
 * MVP 阶段标题与描述写死，后续接入 AI 文案时仅需替换本类逻辑。</p>
 */
public class SalesAgent implements Agent {

    @Override
    public Context run(Context ctx) {

        // 标题（先写死，后续接AI）
        ctx.title = "意式极简羊绒大衣";

        // 描述（先写死）
        ctx.description = "高端双面羊绒面料，极简设计，适合秋冬高端市场";

        // 价格（固定）
        ctx.price = new BigDecimal("299");

        // 商品链接
        ctx.link = "https://deepay.link/" + ctx.chainCode;

        return ctx;
    }
}
