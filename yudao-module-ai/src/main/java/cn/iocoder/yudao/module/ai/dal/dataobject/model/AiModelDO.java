package cn.iocoder.yudao.module.ai.dal.dataobject.model;

import cn.iocoder.yudao.module.ai.enums.model.AiModelInputModalityEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.StringListTypeHandler;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * AI 模型 DO
 *
 * 默认模型：{@link #status} 为开启，并且 {@link #sort} 排序第一
 *
 * @author fansili
 * @since 2024/4/24 19:39
 */
@TableName(value = "ai_model", autoResultMap = true)
@KeySequence("ai_model_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * API 秘钥编号
     *
     * 关联 {@link AiApiKeyDO#getId()}
     */
    private Long keyId;
    /**
     * 模型名称
     */
    private String name;
    /**
     * 模型标志
     */
    private String model;
    /**
     * 平台
     *
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;
    /**
     * 类型
     *
     * 枚举 {@link AiModelTypeEnum}
     */
    private Integer type;

    /**
     * 排序值
     */
    private Integer sort;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    // ========== 对话配置 ==========

    /**
     * 温度参数
     *
     * 用于调整生成回复的随机性和多样性程度：较低的温度值会使输出更收敛于高频词汇，较高的则增加多样性
     */
    private Double temperature;
    /**
     * 单条回复的最大 Token 数量
     */
    private Integer maxTokens;
    /**
     * 上下文的最大 Message 数量
     */
    private Integer maxContexts;
    /**
     * 输入模态数组
     *
     * 枚举 {@link AiModelInputModalityEnum}，仅 {@link AiModelTypeEnum#CHAT} 使用；为空时视为仅支持 text
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> inputModalities;

    /**
     * 思考配置
     *
     * 仅 {@link AiModelTypeEnum#CHAT} 使用；为 null 时不管理思考，保持平台原有行为
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ReasoningConfig reasoningConfig;

    /**
     * 思考配置
     *
     * @author 芋道源码
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReasoningConfig {

        /**
         * 档位数组，从低到高
         */
        private List<ReasoningLevel> levels;
        /**
         * 默认档位，必须在 {@link #levels} 中
         */
        private String defaultLevel;

    }

    /**
     * 思考档位
     *
     * @author 芋道源码
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReasoningLevel {

        /**
         * 档位值，off 表示关闭思考
         */
        private String level;
        /**
         * 思考参数，JSON 对象（任意层级都不允许 null）
         *
         * 只读：使用时通过 AiReasoningUtils#getReasoningOptions 深拷贝
         */
        private Map<String, Object> options;

    }

}
