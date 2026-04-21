package cn.iocoder.yudao.module.deepay.agent;

import java.math.BigDecimal;
import java.util.List;

/**
 * 全流程唯一数据载体。
 *
 * <p>所有 Agent 只读 / 只写本对象的字段，相互之间不直接调用，从而保持简单与可替换性。
 * 后续接入真实 AI / 支付时只需替换对应 Agent，无需改动其他部分。</p>
 */
public class Context {

    /** 用户输入的一句话需求，例如"极简羊绒大衣" */
    public String prompt;

    /** DesignAgent 输出的候选图片 URL 列表（MVP 固定 3 张） */
    public List<String> images;

    /** DecisionAgent 从 images 中选中的图片 URL */
    public String selectedImage;

    /** ChainAgent 生成并落库的 6 位链码，商品唯一标识 */
    public String chainCode;

    /** FinanceAgent 生成的收款 IBAN（MVP 为 mock 字符串） */
    public String iban;

    /** ImaAgent 创建的 ima 知识库 ID（同步失败时为 null） */
    public String imaKbId;

    /** SalesAgent 生成的商品标题 */
    public String title;

    /** SalesAgent 生成的商品描述 */
    public String description;

    /** SalesAgent 设定的商品价格 */
    public BigDecimal price;

    /** SalesAgent 生成的商品链接 */
    public String link;

}
