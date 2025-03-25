package cn.iocoder.yudao.module.ai.dal.dataobject.workflow;

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
    // TODO @lesan：要不换成 code？主要想，和 bpm 工作流，有点区分，字段上。
    private String definitionKey;

    // TODO @lesan：graph 用这个如何？发现大家貌似更爱用这个字段哈。图！
    /**
     * 工作流模型 JSON 数据
     */
    private String model;

}
