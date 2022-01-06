package cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.task;

import cn.iocoder.yudao.adminserver.modules.bpm.enums.task.ProcessInstanceResultEnum;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.task.ProcessInstanceStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmProcessInstanceExtDO extends BaseDO {

    /**
     * 发起流程的用户编号
     *
     * 冗余 {@link HistoricProcessInstance#getStartUserId()}
     */
    private Long userId;
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
     * 流程实例的状态
     *
     * 枚举 {@link ProcessInstanceStatusEnum}
     */
    private Integer status;
    /**
     * 结果
     *
     * 枚举 {@link ProcessInstanceResultEnum}
     */
    private Integer result;

}
