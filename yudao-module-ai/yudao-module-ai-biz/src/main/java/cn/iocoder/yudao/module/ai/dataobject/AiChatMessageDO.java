package cn.iocoder.yudao.module.ai.dataobject;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * ai 聊天 message
 *
 * @fansili
 * @since v1.0
 */
@Data
@Accessors(chain = true)
public class AiChatMessageDO {

    /**
     * 编号，作为每条聊天记录的唯一标识符
     */
    private Long id;

    /**
     * 聊天ID，关联到特定的会话或对话
     */
    private Long chatConversationId;

    /**
     * 角色ID，用于标识发送消息的用户或系统的身份
     */
    private Long userId;

    /**
     * 消息具体内容，存储用户的发言或者系统响应的文字信息
     */
    private String message;

    /**
     * 消息类型，枚举值可能包括'system'(系统消息)、'user'(用户消息)和'assistant'(助手消息)
     */
    private String messageType;

    /**
     * 在生成消息时采用的Top-K采样大小，
     * 表示模型生成回复时考虑的候选项集合的大小
     */
    private Double topK;

    /**
     * Top-P核采样方法的概率阈值，
     * 在语言模型生成过程中控制采样的过滤标准
     */
    private Double topP;

    /**
     * 温度参数，用于调整生成回复的随机性和多样性程度，
     * 较低的温度值会使输出更收敛于高频词汇，较高的则增加多样性
     */
    private Double temperature;

    /**
     * 创建该记录的操作员ID
     */
    private Long createdBy;

    /**
     * 记录创建的时间戳
     */
    private Date createdTime;

    /**
     * 最后更新该记录的操作员ID
     */
    private Long updatedBy;

    /**
     * 记录最后更新的时间戳
     */
    private Date updatedTime;
}
