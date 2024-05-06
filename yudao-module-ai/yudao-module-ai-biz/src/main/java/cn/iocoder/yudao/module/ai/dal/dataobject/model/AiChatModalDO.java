package cn.iocoder.yudao.module.ai.dal.dataobject.model;

import cn.iocoder.yudao.framework.ai.AiPlatformEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * AI 聊天模型 DO
 *
 * @author fansili
 * @time 2024/4/24 19:39
 * @since 1.0
 */
@Data
@Accessors(chain = true)
@TableName("ai_chat_modal")
public class AiChatModalDO extends BaseDO {

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
    private Long key_id;
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
     * 排序值
     */
    private Integer sort;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    // ========== 会话配置 ==========

    /**
     * 温度参数
     *
     * 用于调整生成回复的随机性和多样性程度：较低的温度值会使输出更收敛于高频词汇，较高的则增加多样性
     */
    private Double temperature;
    /**
     * 单条回复的 Token 数量
     */
    private Integer maxTokens;

    // TODO 芋艿：到底使用 max_context、还是 contextCount，待定！一个是轮次，一个是长度数量；貌似轮次更常用一点；

}
