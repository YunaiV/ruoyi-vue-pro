package cn.iocoder.yudao.module.ai.dal.dataobject.model;

import cn.iocoder.yudao.framework.ai.chat.messages.MessageType;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * AI 聊天角色 DO
 *
 * @author fansili
 * @since 2024/4/24 19:39
 */
@TableName(value = "ai_chat_role", autoResultMap = true)
@KeySequence("ai_chat_role_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRoleDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色头像
     */
    private String avatar;
    /**
     * 角色分类
     */
    private String category;
    /**
     * 角色描述
     */
    private String description;
    /**
     * 角色欢迎语
     */
    private String welcomeMessage;
    /**
     * 角色设定（消息）
     */
    private String systemMessage;

    /**
     * 用户编号
     *
     * 关联 AdminUserDO 的 userId 字段
     */
    private Long userId;

    /**
     * 模型编号
     *
     * 关联 {@link AiChatModalDO#getId()} 字段
     */
    private String modelId;

    /**
     * 是否公开
     *
     * true - 公开；false - 私有
     */
    private Boolean publicStatus;

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

}
