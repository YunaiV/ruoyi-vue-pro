package cn.iocoder.yudao.module.mes.controller.admin.wm.batch.vo;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 批次生成请求 VO
 *
 * @author 芋道源码
 */
@Data
public class MesWmBatchGenerateReqVO {

    /**
     * 物料ID
     */
    @NotNull(message = "物料 ID 不能为空")
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
     * 供应商 ID
     */
    private Long vendorId;
    /**
     * 客户 ID
     */
    private Long clientId;

    /**
     * 采购订单编号
     */
    private String purchaseOrderCode;
    /**
     * 销售订单编号
     */
    private String salesOrderCode;

    /**
     * 生产工单 ID
     */
    private Long workOrderId;
    /**
     * 生产任务 ID
     */
    private Long taskId;
    /**
     * 工作站 ID
     */
    private Long workstationId;
    /**
     * 工具 ID
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

}
