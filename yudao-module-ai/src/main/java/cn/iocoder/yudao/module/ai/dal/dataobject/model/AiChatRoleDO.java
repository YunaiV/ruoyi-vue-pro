package cn.iocoder.yudao.module.ai.dal.dataobject.model;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.framework.mybatis.core.type.StringListTypeHandler;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

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
     * 角色设定
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
     * 关联 {@link AiModelDO#getId()} 字段
     */
    private Long modelId;

    /**
     * 引用的知识库编号列表
     *
     * 关联 {@link AiKnowledgeDO#getId()} 字段
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> knowledgeIds;
    /**
     * 引用的工具编号列表
     *
     * 关联 {@link AiToolDO#getId()} 字段
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> toolIds;
    /**
     * 引用的 MCP Client 名字列表
     *
     * 关联 spring.ai.mcp.client 下的名字
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> mcpClientNames;

    /**
     * 是否公开
     *
     * 1. true - 公开；由管理员在【角色管理】所创建
     * 2. false - 私有；由个人在【我的角色】所创建
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
