package cn.iocoder.yudao.module.mes.dal.dataobject.wm.productissue;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmProductIssueStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 领料出库单 DO
 */
@TableName("mes_wm_product_issue")
@KeySequence("mes_wm_product_issue_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmProductIssueDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 领料单编号
     */
    private String code;
    /**
     * 领料单名称
     */
    private String name;
    /**
     * 工作站 ID
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 生产工单 ID
     *
     * 关联 {@link MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;
    // TODO @芋艿：【疑问，不要动】这个字段的来源？
    /**
     * 生产任务 ID
     *
     * 关联 {@link MesProTaskDO#getId()}
     */
    private Long taskId;
    // TODO @芋艿：【疑问，不要动】这个字段的更新时间；
    /**
     * 领料日期
     */
    private LocalDateTime issueDate;
    /**
     * 需求时间
     */
    private LocalDateTime requiredTime;
    /**
     * 状态
     *
     * 枚举 {@link MesWmProductIssueStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
