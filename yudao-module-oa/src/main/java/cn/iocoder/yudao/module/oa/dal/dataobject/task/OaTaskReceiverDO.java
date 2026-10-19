package cn.iocoder.yudao.module.oa.dal.dataobject.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.task.OaTaskStatusEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 任务接收人 DO
 *
 * @author 芋道源码
 */
@TableName("oa_task_receiver")
@KeySequence("oa_task_receiver_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaTaskReceiverDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 任务编号
     *
     * 关联 {@link OaTaskDO#getId()}
     */
    private Long taskId;
    /**
     * 接收人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;
    /**
     * 任务状态
     *
     * 枚举 {@link OaTaskStatusEnum}
     */
    private Integer status;

}
