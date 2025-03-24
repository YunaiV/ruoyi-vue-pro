package cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo;

import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.outbound.OutboundStatus;

/**
 * @table-fields : source_item_id,outbound_status,actual_quantity,bin_id,product_id,plan_quantity,id,outbound_id
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

    @Schema(description = "实际出库量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer actualQuantity;

    @Schema(description = "来源详情ID", example = "11448")
    private Long sourceItemId;

    @Schema(description = "出库状态 ; OutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库", example = "")
    @InEnum(OutboundStatus.class)
    private Integer outboundStatus;

    @Schema(description = "计划出库量", example = "")
    private Integer planQuantity;

    @Schema(description = "出库库位ID", example = "")
    private Long binId;
}
