package cn.iocoder.yudao.module.bpm.dal.dataobject.task;

import cn.iocoder.yudao.adminserver.modules.bpm.enums.task.BpmProcessInstanceResultEnum;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.Date;
import java.util.Map;

/**
 * Bpm 流程实例的拓展表
 * 主要解决 Activiti {@link ProcessInstance} 和 {@link HistoricProcessInstance} 不支持拓展字段，所以新建拓展表
 *
 * @author 芋道源码
 */
@TableName(value = "bpm_process_instance_ext", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
public class BpmProcessInstanceExtDO extends BaseDO {

    /**
     * 编号，自增
     */
    @TableId
    private Long id;
    /**
     * 发起流程的用户编号
     *
     * 冗余 {@link HistoricProcessInstance#getStartUserId()}
     */
    private Long startUserId;
    /**
     * 流程实例的名字
     *
     * 冗余 {@link ProcessInstance#getName()} 为了筛选
     */
    private String name;
    /**
     * 流程实例的编号
     *
     * 关联 {@link ProcessInstance#getId()}
     */
    private String processInstanceId;
    /**
     * 流程定义的编号
     *
     * 关联 {@link ProcessDefinition#getId()}
     */
    private String processDefinitionId;
    /**
     * 流程分类
     *
     * 冗余 {@link ProcessDefinition#getCategory()}
     * 数据字典 bpm_model_category
     */
    private String category;
    /**
     * 流程实例的状态
     *
     * 枚举 {@link BpmProcessInstanceStatusEnum}
     */
    private Integer status;
    /**
     * 流程实例的结果
     *
     * 枚举 {@link BpmProcessInstanceResultEnum}
     */
    private Integer result;
    /**
     * 结束时间
     *
     * 冗余 {@link HistoricProcessInstance#getEndTime()}
     */
    private Date endTime;

    /**
     * 提交的表单值
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> formVariables;

}
