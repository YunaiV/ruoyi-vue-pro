package cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.enums.pro.MesProFeedbackStatusEnum;
import cn.iocoder.yudao.module.mes.enums.pro.MesProFeedbackTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 生产报工 DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_feedback")
@KeySequence("mes_pro_feedback_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProFeedbackDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 报工单编号
     */
    private String code;
    /**
     * 报工类型
     *
     * 字典 {@link DictTypeConstants#MES_PRO_FEEDBACK_TYPE}
     * 枚举 {@link MesProFeedbackTypeEnum}
     */
    private Integer type;
    // TODO @芋艿：这个字段，暂时没使用到；
    /**
     * 报工途径
     *
     * 字典类型 mes_pro_feedback_channel
     */
    private String channel;
    /**
     * 报工时间
     */
    private LocalDateTime feedbackTime;
    /**
     * 工作站编号
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 工艺路线编号
     *
     * 关联 {@link MesProRouteDO#getId()}
     */
    private Long routeId;
    /**
     * 工序编号
     *
     * 关联 {@link MesProProcessDO#getId()}
     */
    private Long processId;
    /**
     * 生产工单编号
     *
     * 关联 {@link MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;
    // TODO @芋艿：这里待关联；
    /**
     * 生产任务编号
     */
    private Long taskId;
    /**
     * 产品物料编号（冗余自任务）
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 过期日期
     */
    private LocalDateTime expireDate;
    /**
     * 生产批号
     *
     * TODO @芋艿：预留字段，当前未启用
     */
    private String lotNumber;
    /**
     * 排产数量
     */
    private BigDecimal scheduledQuantity;
    /**
     * 本次报工数量
     */
    private BigDecimal feedbackQuantity;
    /**
     * 合格品数量
     */
    private BigDecimal qualifiedQuantity;
    /**
     * 不良品数量
     */
    private BigDecimal unqualifiedQuantity;
    /**
     * 待检测数量
     */
    private BigDecimal uncheckQuantity;
    /**
     * 工废数量
     */
    private BigDecimal laborScrapQuantity;
    /**
     * 料废数量
     */
    private BigDecimal materialScrapQuantity;
    /**
     * 其他废品数量
     */
    private BigDecimal otherScrapQuantity;
    /**
     * 报工用户编号
     *
     * 关联 AdminUserDO#getId()
     */
    private Long feedbackUserId;
    /**
     * 审核用户编号
     *
     * 关联 AdminUserDO#getId()
     */
    private Long approveUserId;
    /**
     * 状态
     *
     * 字典 {@link DictTypeConstants#MES_PRO_FEEDBACK_STATUS}
     * 枚举 {@link MesProFeedbackStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
