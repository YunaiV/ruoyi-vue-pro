package cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.tm.tool.MesTmToolDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 批次管理 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_batch")
@KeySequence("mes_wm_batch_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmBatchDO extends BaseDO {

    /**
     * 批次ID
     */
    @TableId
    private Long id;
    /**
     * 批次编码
     */
    private String code;
    /**
     * 物料ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 生产日期
     */
    private LocalDateTime produceDate;
    /**
     * 有效期
     */
    private LocalDateTime expireDate;
    /**
     * 入库日期
     */
    private LocalDateTime receiptDate;
    /**
     * 供应商ID
     *
     * 关联 {@link MesMdVendorDO#getId()}
     */
    private Long vendorId;
    /**
     * 客户ID
     *
     * 关联 {@link MesMdClientDO#getId()}
     */
    private Long clientId;

    /**
     * 销售订单编号
     */
    private String salesOrderCode;
    /**
     * 采购订单编号
     */
    private String purchaseOrderCode;
    /**
     * 生产工单ID
     *
     * 关联 {@link MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;
    /**
     * 生产任务ID
     *
     * 关联 {@link MesProTaskDO#getId()}
     */
    private Long taskId;
    /**
     * 工作站ID
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 工具ID
     *
     * 关联 {@link MesTmToolDO#getId()}
     */
    private Long toolId;
    /**
     * 模具 ID
     */
    private Long moldId;
    /**
     * 生产批号
     */
    private String lotNumber;
    /**
     * 质量状态
     */
    private Integer qualityStatus;
    /**
     * 备注
     */
    private String remark;

}
