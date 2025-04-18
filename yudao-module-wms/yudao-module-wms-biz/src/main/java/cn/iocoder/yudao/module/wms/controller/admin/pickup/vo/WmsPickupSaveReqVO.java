package cn.iocoder.yudao.module.wms.controller.admin.pickup.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.util.List;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo.WmsPickupItemSaveReqVO;

/**
 * @table-fields : code,upstream_bill_id,id,upstream_bill_code,warehouse_id,upstream_bill_type
 */
@Schema(description = "管理后台 - 拣货单新增/修改 Request VO")
@Data
public class WmsPickupSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "8396")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22380")
    private Long warehouseId;

    @Schema(description = "详情清单", example = "")
    private List<WmsPickupItemSaveReqVO> itemList;

    @Schema(description = "单据号", example = "")
    private String code;

    @Schema(description = "来源单据ID", example = "")
    private Long upstreamBillId;

    @Schema(description = "来源单据号", example = "")
    private String upstreamBillCode;

    @Schema(description = "来源单据类型", example = "")
    private Integer upstreamBillType;
}
