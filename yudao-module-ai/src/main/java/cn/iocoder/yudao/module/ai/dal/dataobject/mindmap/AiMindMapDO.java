package cn.iocoder.yudao.module.ai.dal.dataobject.mindmap;

import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.requestlog.AiRequestLogDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 思维导图 DO
 *
 * @author xiaoxin
 */
@TableName(value = "ai_mind_map")
@KeySequence("ai_mind_map_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiMindMapDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 用户编号
     * <p>
     * 关联 AdminUserDO 的 userId 字段
     */
    private Long userId;

    /**
     * 平台
     * <p>
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;
    /**
     * 模型编号
     *
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;
    /**
     * 模型
     */
    private String model;

    /**
     * 生成内容提示
     */
    private String prompt;

    /**
     * 生成的内容
     */
    private String generatedContent;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 请求编号
     *
     * 关联 {@link AiRequestLogDO#getRequestId()}
     */
    private String requestId;
    /**
     * 输入 Token 数
     *
     * 未知为 null
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
     * 输入、输出都已知时才有值，否则为 null
     */
    private Long totalTokens;

}