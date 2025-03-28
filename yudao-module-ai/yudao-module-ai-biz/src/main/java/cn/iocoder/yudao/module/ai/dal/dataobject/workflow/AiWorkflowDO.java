package cn.iocoder.yudao.module.ai.dal.dataobject.workflow;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 工作流 DO
 *
 * @author lesan
 */
@TableName(value = "ai_workflow", autoResultMap = true)
@KeySequence("ai_workflow") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiWorkflowDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 工作流名称
     */
    private String name;
    /**
     * 工作流标识
     */
    private String code;

    /**
     * 工作流模型 JSON 数据
     */
    private String graph;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
