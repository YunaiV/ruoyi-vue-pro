package cn.iocoder.yudao.module.ai.dal.dataobject.knowledge;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.List;

/**
 * AI 知识库 DO
 *
 * @author xiaoxin
 */
@TableName(value = "ai_knowledge", autoResultMap = true)
@Data
public class AiKnowledgeDO extends BaseDO {

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
     * 知识库名称
     */
    private String name;
    /**
     * 知识库描述
     */
    private String description;
    // TODO @新：如果全部可见，需要怎么设置？
    /**
     * 可见权限,只能选择哪些人可见
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> visibilityPermissions;
    /**
     * 嵌入模型编号
     */
    private Long modelId;
    /**
     * 模型标识
     */
    private String model;
    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
}
