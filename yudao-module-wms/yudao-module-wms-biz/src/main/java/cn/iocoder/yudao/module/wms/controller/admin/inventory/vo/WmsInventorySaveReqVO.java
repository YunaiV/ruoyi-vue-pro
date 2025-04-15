package cn.iocoder.yudao.module.wms.controller.admin.inventory.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductSaveReqVO;
import cn.iocoder.yudao.module.wms.enums.inventory.WmsInventoryAuditStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * @table-fields : no,audit_status,id,creator_remark,warehouse_id
 */
@Schema(description = "管理后台 - 盘点新增/修改 Request VO")
@Data
public class WmsInventorySaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10689")
    private Long id;

    @Schema(description = "单据号")
    private String no;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26854")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "WMS盘点单审批状态 ; WmsInventoryAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @InEnum(WmsInventoryAuditStatus.class)
    private Integer auditStatus;

    @Schema(description = "产品详情清单", example = "")
    private List<WmsInventoryProductSaveReqVO> productItemList;

    @Schema(description = "库位详情清单", example = "")
    private List<WmsInventoryBinSaveReqVO> binItemList;

    @Schema(description = "创建者备注", example = "")
    private String creatorRemark;
}
