package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户画像表 deepay_customer_profile（Phase 6 — 记忆中枢）
 *
 * <p>每位客户一条记录，记录其品类偏好、风格权重、市场方向等信息。
 * 由 CustomerProfileAgent 读取并在每次交互后更新，是系统"记住客户"的核心存储。</p>
 *
 * <p>验收：
 * <ul>
 *   <li>能存 — INSERT 时 customer_id 唯一</li>
 *   <li>能查 — 按 customer_id 精确查询</li>
 *   <li>一个客户一条记录 — unique(customer_id)</li>
 * </ul>
 * </p>
 */
@TableName("deepay_customer_profile")
@Data
public class DeepayCustomerProfileDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 客户 ID（唯一键，来自 deepay_client.id 或外部用户系统）。
     * 每位客户仅一条画像记录；使用 INSERT ... ON DUPLICATE KEY UPDATE 保持幂等。
     */
    private Long customerId;

    /**
     * 一级品类（如 外套、内裤、连衣裙）。
     * 由 SmartQuestionAgent 首次填充，用户后续选择时由 PreferenceLearningAgent 更新。
     */
    private String categoryLevel1;

    /**
     * 二级品类（如 羽绒服、蕾丝内裤）。
     * 细化分类，可留空。
     */
    private String categoryLevel2;

    /**
     * 风格权重 JSON，例如：
     * <pre>{"minimalist":0.8,"streetwear":0.3,"luxury":0.5}</pre>
     * 每次用户选择图片时，对应风格权重 +0.1，由 PreferenceLearningAgent 更新。
     */
    private String styleWeights;

    /**
     * 价格带：LOW（低端）/ MID（中端）/ HIGH（高端）。
     * SmartQuestionAgent 首次询问填入，后续可动态调整。
     */
    private String priceLevel;

    /**
     * 目标市场：CN（国内）/ EU（欧洲）/ US（北美）/ ME（中东）。
     * 影响 DesignAgent prompt 的区域化描述。
     */
    private String market;

    /**
     * 目标年龄段：YOUNG（18-30）/ MIDDLE（30-50）/ ELDER（50+）。
     */
    private String targetAge;

    /**
     * 目标性别：MALE / FEMALE / UNISEX。
     */
    private String gender;

    /**
     * 画像置信度（0.00~1.00）：
     * <ul>
     *   <li>0.00~0.59 — 数据不足，需触发 SmartQuestionAgent 补充</li>
     *   <li>0.60~0.79 — 基本可用，可跳过问卷</li>
     *   <li>0.80~1.00 — 高置信度，系统自动运行</li>
     * </ul>
     * 初始创建时为 0；SmartQuestionAgent 完整回答后设为 0.6；
     * 每次 PreferenceLearningAgent 更新后 +0.05，上限 1.0。
     */
    private BigDecimal confidenceScore;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
