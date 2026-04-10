package cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.batchconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 物料批次属性配置新增/修改 Request VO")
@Data
public class MesMdItemBatchConfigSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "69")
    @NotNull(message = "物料编号不能为空")
    private Long itemId;

    @Schema(description = "批次属性-生产日期", example = "true")
    private Boolean produceDateFlag;

    @Schema(description = "批次属性-有效期", example = "true")
    private Boolean expireDateFlag;

    @Schema(description = "批次属性-入库日期", example = "false")
    private Boolean receiptDateFlag;

    @Schema(description = "批次属性-供应商", example = "false")
    private Boolean vendorFlag;

    @Schema(description = "批次属性-客户", example = "false")
    private Boolean clientFlag;

    @Schema(description = "批次属性-销售订单编号", example = "false")
    private Boolean salesOrderCodeFlag;

    @Schema(description = "批次属性-采购订单编号", example = "false")
    private Boolean purchaseOrderCodeFlag;

    @Schema(description = "批次属性-生产工单", example = "false")
    private Boolean workorderFlag;

    @Schema(description = "批次属性-生产任务", example = "false")
    private Boolean taskFlag;

    @Schema(description = "批次属性-工作站", example = "false")
    private Boolean workstationFlag;

    @Schema(description = "批次属性-工具", example = "false")
    private Boolean toolFlag;

    @Schema(description = "批次属性-模具", example = "false")
    private Boolean moldFlag;

    @Schema(description = "批次属性-生产批号", example = "true")
    private Boolean lotNumberFlag;

    @Schema(description = "批次属性-质量状态", example = "false")
    private Boolean qualityStatusFlag;

}
