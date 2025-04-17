package cn.iocoder.yudao.module.wms.controller.admin.inventory.vo;

import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,creator,update_time,code,create_time,id,audit_status,creator_remark,warehouse_id,updater
 */
@Schema(description = "管理后台 - 盘点 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInventoryRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10689")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26854")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "WMS盘点单审批状态 ; WmsInventoryAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("WMS盘点单审批状态")
    private Integer auditStatus;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "产品详情清单", example = "")
    @ExcelProperty("产品详情清单")
    private List<WmsInventoryProductRespVO> productItemList;

    @Schema(description = "库位详情清单", example = "")
    @ExcelProperty("库位详情清单")
    private List<WmsInventoryBinRespVO> binItemList;

    @Schema(description = "创建者", example = "")
    @ExcelProperty("创建者")
    private String creator;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;

    @Schema(description = "仓库", example = "")
    @ExcelProperty("仓库")
    private WmsWarehouseSimpleRespVO warehouse;

    @Schema(description = "创建者备注", example = "")
    @ExcelProperty("创建者备注")
    private String creatorRemark;

    @Schema(description = "单据号", example = "")
    @ExcelProperty("单据号")
    private String code;
}
