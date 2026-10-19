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
 * OA 任务反馈日志 DO
 *
 * @author 芋道源码
 */
@TableName("oa_task_log")
@KeySequence("oa_task_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaTaskLogDO extends BaseDO {

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
     * 反馈人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;
    /**
     * 变更后的状态
     *
     * 枚举 {@link OaTaskStatusEnum}
     */
    private Integer status;
    /**
     * 反馈内容
     */
    private String content;

}
