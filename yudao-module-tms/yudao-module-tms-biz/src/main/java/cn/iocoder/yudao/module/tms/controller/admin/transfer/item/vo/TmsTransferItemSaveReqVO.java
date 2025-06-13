package cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Schema(description = "管理后台 - 调拨单明细新增/修改 Request VO")
@Data
public class TmsTransferItemSaveReqVO {
    @Schema(description = "调拨单明细编号")
    @NotNull(groups = {Validation.OnUpdate.class}, message = "更新时，调拨单明细id不能为空")
    @Null(groups = {Validation.OnCreate.class}, message = "创建时，调拨单明细id需为空")
    private Long id;

    @Schema(description = "产品id")
    @NotNull(message = "调拨明细产品id不能为空")
    private Long productId;

    @Schema(description = "数量")
    @NotNull(message = "调拨明细数量不能为空")
    private Integer qty;

    @Schema(description = "箱数")
    private Integer boxQty;

    @Schema(description = "库存公司ID")
    private Long stockCompanyId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "库存归属部门ID")
    private Long deptId;
}