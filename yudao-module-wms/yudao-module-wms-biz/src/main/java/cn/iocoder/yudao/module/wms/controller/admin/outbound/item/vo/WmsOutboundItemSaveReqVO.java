package cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo;

import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;

/**
 * @table-fields : source_item_id,actual_qty,company_id,outbound_status,bin_id,plan_qty,product_id,id,dept_id,outbound_id
 */
@Schema(description = "管理后台 - 出库单详情新增/修改 Request VO")
@Data
public class WmsOutboundItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "27153")
    @NotNull(message = "主键不能为空", groups = { ValidationGroup.update.class })
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6602")
    private Long outboundId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20572")
    @NotNull(message = "标准产品ID不能为空", groups = { ValidationGroup.create.class, ValidationGroup.update.class })
    private Long productId;

    @Schema(description = "来源详情ID", example = "11448")
    private Long sourceItemId;

    @Schema(description = "WMS出库状态 ; WmsOutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库", example = "")
    @InEnum(WmsOutboundStatus.class)
    private Integer outboundStatus;

    @Schema(description = "出库库位ID，在创建时指定；bin_id 和 inbount_item_id 需要指定其中一个，优先使用 inbount_item_id", example = "")
    private Long binId;

    @Schema(description = "实际出库量", example = "")
    private Integer actualQty;

    @Schema(description = "计划出库量", example = "")
    private Integer planQty;

    @Schema(description = "库存财务公司ID", example = "")
    private Long companyId;

    @Schema(description = "库存归属部门ID", example = "")
    private Long deptId;
}
