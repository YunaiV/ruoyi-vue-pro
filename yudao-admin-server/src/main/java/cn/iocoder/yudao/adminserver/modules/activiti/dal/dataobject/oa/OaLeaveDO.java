package cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.oa;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 请假申请 DO
 *
 * @author 芋艿
 */
@TableName("oa_leave")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OaLeaveDO extends BaseDO {

    /**
     * 请假表单主键
     */
    @TableId
    private Long id;
    /**
     * 流程id
     */
    private String processInstanceId;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 申请人id
     */
    private String userId;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 请假类型
     */
    private String leaveType;
    /**
     * 原因
     */
    private String reason;
    /**
     * 申请时间
     */
    private Date applyTime;

}
