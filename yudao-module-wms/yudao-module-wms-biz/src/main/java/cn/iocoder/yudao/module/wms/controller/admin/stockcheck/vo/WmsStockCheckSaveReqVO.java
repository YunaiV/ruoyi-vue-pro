package cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo.WmsStockCheckBinSaveReqVO;
import cn.iocoder.yudao.module.wms.enums.stock.check.WmsStockCheckAuditStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @table-fields : code,remark,id,audit_status,warehouse_id
 */
@Schema(description = "管理后台 - 盘点新增/修改 Request VO")
@Data
public class WmsStockCheckSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10689")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26854")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "WMS盘点单审批状态 ; WmsStockCheckAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过 , 5-作废", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @InEnum(WmsStockCheckAuditStatus.class)
    private Integer auditStatus;

    @Schema(description = "库位详情清单", example = "")
    private List<WmsStockCheckBinSaveReqVO> binItemList;

    @Schema(description = "单据号", example = "")
    private String code;

    @Schema(description = "创建者备注", example = "")
    private String remark;
}
