package cn.iocoder.yudao.module.bpm.dal.dataobject.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程抄送 DO
 *
 * @author kyle
 * @since 2024-01-22
 */
@TableName(value = "bpm_process_instance_copy", autoResultMap = true)
@KeySequence("bpm_process_instance_copy_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmProcessInstanceCopyDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 发起人 Id
     *
     * 冗余 ProcessInstance 的 startUserId 字段
     */
    private Long startUserId;
    /**
     * 流程名
     *
     * 冗余 ProcessInstance 的 name 字段
     */
    private String processInstanceName;
    /**
     * 流程实例的编号
     *
     * 关联 ProcessInstance 的 id 属性
     */
    private String processInstanceId;
    /**
     * 流程分类
     *
     * 冗余 ProcessInstance 的 category 字段
     */
    private String category;
    /**
     * 流程活动编号
     * <p/>
     * 对应 BPMN XML 节点编号，用于查询抄送节点的表单字段权限
     * 这里冗余的原因：如果是钉钉易搭的抄送节点 (ServiceTask)，使用 taskId 可能查不到对应的 activityId
     */
    private String activityId;
    /**
     * 任务主键
     * 关联 Task 的 id 属性
     */
    private String taskId;
    /**
     * 任务名称
     *
     * 冗余 Task 的 name 属性
     */
    private String taskName;

    /**
     * 用户编号（被抄送的用户编号）
     *
     * 关联 system_users 的 id 属性
     */
    private Long userId;

}
