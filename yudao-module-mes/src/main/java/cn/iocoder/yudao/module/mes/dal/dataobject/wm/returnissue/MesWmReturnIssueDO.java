package cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnissue;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmReturnIssueStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 生产退料单 DO
 */
@TableName("mes_wm_return_issue")
@KeySequence("mes_wm_return_issue_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmReturnIssueDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 退料单编号
     */
    private String code;
    /**
     * 退料单名称
     */
    private String name;
    /**
     * 生产工单 ID
     *
     * 关联 {@link MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;
    /**
     * 工作站 ID
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 退料类型
     *
     * 字典 {@link DictTypeConstants#MES_WM_RETURN_ISSUE_TYPE}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.MesWmReturnIssueTypeEnum}
     */
    private Integer type;
    /**
     * 退料日期
     */
    private LocalDateTime returnDate;
    /**
     * 状态
     *
     * 字典 {@link DictTypeConstants#MES_WM_RETURN_ISSUE_STATUS}
     * 枚举 {@link MesWmReturnIssueStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
