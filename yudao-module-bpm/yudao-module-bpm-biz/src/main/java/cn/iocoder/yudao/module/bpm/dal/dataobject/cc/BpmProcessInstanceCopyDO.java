package cn.iocoder.yudao.module.bpm.dal.dataobject.cc;

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
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
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
     * <p>
     * 关联 system_users 的 id 属性
     */
    private Long startUserId;
    /**
     * 流程名
     * <p>
     * 冗余字段
     */
    private String processInstanceName;
    /**
     * 流程实例的编号
     * <p>
     * 关联 ProcessInstance 的 id 属性
     */
    private String processInstanceId;

    /**
     * 任务主键
     * <p>
     * 关联 task 的 id 属性
     */
    private String taskId;

    /**
     * 任务名称
     * <p>
     * 冗余字段
     */
    private String taskName;

    /**
     * 用户编号
     * <p>
     * 关联 system_users 的 id 属性
     */
    private Long userId;

    /**
     * 抄送原因
     */
    private String reason;

    /**
     * 流程分类
     * <p>
     * 冗余字段
     */
    private String category;

}
