package cn.iocoder.yudao.module.bpm.dal.dataobject.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 流程抄送 DO
 *
 * @author kyle
 * @since 2024-01-22
 */
@TableName(value = "bpm_process_instance_copy", autoResultMap = true)
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
     * 任务主键
     *
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
