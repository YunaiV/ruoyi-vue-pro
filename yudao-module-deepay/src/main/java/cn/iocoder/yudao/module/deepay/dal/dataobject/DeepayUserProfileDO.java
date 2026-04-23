package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户画像表 deepay_user_profile（Phase 6 — 终端用户记忆）
 *
 * <p>与 {@code deepay_customer_profile}（B2B 批发客户）区分：
 * 此表面向终端用户（商家、设计师），以字符串 user_id 作为标识符，
 * 兼容外部系统（微信 openid / 商家账号 / 平台 UID 等）。</p>
 *
 * <p>一个 user_id 对应一条记录，由 {@link cn.iocoder.yudao.module.deepay.agent.MemoryAgent}
 * 读写，实现"记住用户偏好"功能。</p>
 */
@TableName("deepay_user_profile")
@Data
public class DeepayUserProfileDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 外部用户 ID（VARCHAR，兼容微信 openid / 平台 UID 等字符串格式）。
     * 唯一索引 unique(user_id) 保证一用户一条记录。
     */
    private String userId;

    /**
     * 品类：上衣 / 外套 / 内裤 / 裤子 / 连衣裙 等。
     * SmartQuestionAgent 首次填写，PreferenceLearningAgent 持续更新。
     */
    private String category;

    /**
     * 风格偏好：性感 / 工装 / 极简 / 少女 / 运动 / SEXY / MINIMAL 等。
     * 存储最主要的单一风格标签；完整权重见 {@link #styleWeightsJson}。
     */
    private String stylePreference;

    /**
     * 风格权重 JSON（完整版），例如：{"SEXY":0.8,"MINIMAL":0.3}。
     * 由 PreferenceLearningAgent 更新，StyleEngine 读取此字段进行风格组合。
     */
    private String styleWeightsJson;

    /**
     * 性别：MALE / FEMALE / UNISEX。
     */
    private String gender;

    /**
     * 年龄段：YOUNG（18-30）/ MIDDLE（30-50）/ ELDER（50+）。
     */
    private String ageGroup;

    /**
     * 价格带：LOW / MID / HIGH。
     */
    private String priceRange;

    /**
     * 目标市场：欧美（EU/US）/ 中东（ME）/ 国内（CN）。
     * 建议存储为枚举值：CN / EU / US / ME。
     */
    private String market;

    /**
     * 画像置信度（0.00~1.00）。
     * 初始为 0；QuestionAgent 填完基础字段后升至 0.6；
     * 每次 PreferenceLearningAgent 更新后 +0.05，上限 1.0。
     */
    private java.math.BigDecimal confidence;

    private LocalDateTime updatedAt;

    private LocalDateTime createdAt;

}
