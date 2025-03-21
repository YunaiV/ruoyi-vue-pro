package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.inbound.InboundStatus;

/**
 * @table-fields : inbound_id,source_item_id,left_quantity,inbound_status,actual_quantity,product_id,plan_quantity,shelved_quantity,id
 */
@Schema(description = "管理后台 - 入库单详情新增/修改 Request VO")
@Data
public class WmsInboundItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30520")
    @NotNull(message = "主键不能为空", groups = { ValidationGroup.update.class })
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29327")
    private Long inboundId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27659")
    @NotNull(message = "标准产品ID不能为空", groups = { ValidationGroup.create.class, ValidationGroup.update.class })
    private Long productId;

    @Schema(description = "计划入库量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer planQuantity;

    @Schema(description = "实际入库量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer actualQuantity;

    @Schema(description = "批次剩余库存，出库后的剩余库存量")
    private Integer leftQuantity;

    @Schema(description = "来源详情ID", example = "30830")
    private Long sourceItemId;

    @Schema(description = "入库状态 ; InboundStatus : 0-未入库 , 1-部分入库 , 2-已入库", example = "")
    @InEnum(InboundStatus.class)
    private Integer inboundStatus;

    @Schema(description = "已上架量，已经拣货到仓位的库存量", example = "")
    private Integer shelvedQuantity;
}
