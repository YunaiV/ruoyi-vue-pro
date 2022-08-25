package cn.iocoder.yudao.module.bpm.dal.dataobject.oa;

import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceResultEnum;
import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * OA 请假申请 DO
 *
 * {@link #day} 请假天数，目前先简单做。一般是分成请假上午和下午，可以是 1 整天，可以是 0.5 半天
 *
 * @author jason
 * @author 芋道源码
 */
@TableName("bpm_oa_leave")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmOALeaveDO extends BaseDO {

    /**
     * 请假表单主键
     */
    @TableId
    private Long id;
    /**
     * 申请人的用户编号
     *
     * 关联 AdminUserDO 的 id 属性
     */
    private Long userId;
    /**
     * 请假类型
     */
    @TableField("`type`")
    private String type;
    /**
     * 原因
     */
    private String reason;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 请假天数
     */
    private Long day;
    /**
     * 请假的结果
     *
     * 枚举 {@link BpmProcessInstanceResultEnum}
     * 考虑到简单，所以直接复用了 BpmProcessInstanceResultEnum 枚举，也可以自己定义一个枚举哈
     */
    private Integer result;

    /**
     * 对应的流程编号
     *
     * 关联 ProcessInstance 的 id 属性
     */
    private String processInstanceId;

}
