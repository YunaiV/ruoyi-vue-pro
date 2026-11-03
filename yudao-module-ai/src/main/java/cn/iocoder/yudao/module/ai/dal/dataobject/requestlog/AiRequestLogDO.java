package cn.iocoder.yudao.module.ai.dal.dataobject.requestlog;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.mindmap.AiMindMapDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.write.AiWriteDO;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogBizTypeEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 请求日志 DO
 *
 * 一行对应一次业务生成尝试（例如说一次聊天回复、一次写作、一次思维导图生成），不承诺对应每一次底层 HTTP 调用
 *
 * @author 芋道源码
 */
@TableName(value = "ai_request_log", autoResultMap = true)
@KeySequence("ai_request_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequestLogDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 请求编号
     *
     * 服务端在调用模型前生成的本地 UUID，唯一；与厂商响应 id 不同
     */
    private String requestId;
    /**
     * 用户编号
     *
     * 关联 AdminUserDO 的 userId 字段，在请求开始时显式捕获
     */
    private Long userId;
    /**
     * 业务类型
     *
     * 枚举 {@link AiRequestLogBizTypeEnum}
     */
    private Integer bizType;
    /**
     * 业务编号
     *
     * 根据 {@link #bizType} 不同，关联：
     * 1. {@link AiRequestLogBizTypeEnum#CHAT}：{@link AiChatMessageDO#getId()} 的 assistant 消息
     * 2. {@link AiRequestLogBizTypeEnum#WRITE}：{@link AiWriteDO#getId()}
     * 3. {@link AiRequestLogBizTypeEnum#MIND_MAP}：{@link AiMindMapDO#getId()}
     */
    private Long bizId;
    /**
     * 平台
     *
     * 枚举 {@link AiPlatformEnum}，调用时快照
     */
    private String platform;
    /**
     * 模型编号
     *
     * 关联 {@link AiModelDO#getId()} 字段，调用时快照
     */
    private Long modelId;
    /**
     * 模型标志
     *
     * 冗余 {@link AiModelDO#getModel()}，调用时快照
     */
    private String model;
    /**
     * 请求状态
     *
     * 枚举 {@link AiRequestLogStatusEnum}
     */
    private Integer status;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 开始时间
     */
    private LocalDateTime beginTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 执行时长，单位：毫秒
     */
    private Long duration;
    /**
     * 输入 Token 数
     *
     * 归一后的完整输入（例如说 Anthropic 包含缓存读取、写入）；未知为 null
     */
    private Long inputTokens;
    /**
     * 输出 Token 数
     *
     * 未知为 null
     */
    private Long outputTokens;
    /**
     * 合计 Token 数
     *
     * 固定为 {@link #inputTokens} + {@link #outputTokens}，仅两者都已知时计算；否则为 null
     */
    private Long totalTokens;
    /**
     * 响应元数据
     *
     * 框架取得的响应 JSON 快照，包括 id、model、usage（含 nativeUsage）、rateLimit、promptMetadata、扩展属性、每个 generation 的 finishReason 等
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> responseMetadata;

}
