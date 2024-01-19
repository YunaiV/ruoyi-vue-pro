package cn.iocoder.yudao.module.bpm.dal.dataobject.cc;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 流程抄送 DO
 *
 * @author kyle
 * @date 2022-05-19 TODO @kyle：@date 不是标准 java doc，可以使用 @since 替代，然后日期是不是不对
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

    // TODO @kyle：字段如果是关联或者冗余，要写下注释。以 processInstanceId 举例子。
    /**
     * 发起人 Id
     */
    private Long startUserId;
    /**
     * 流程名
     */
    private String processInstanceName;
    /**
     * 流程实例的编号
     *
     * 关联 ProcessInstance 的 id 属性
     */
    private String processInstanceId;

    /**
     * 任务主键
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 抄送原因
     */
    private String reason;

    // TODO @kyle：这个字段，可以用 category 简化点
    /**
     * 流程分类
     */
    private String processDefinitionCategory;

}
