package cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo;

import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,creator,create_time,bin_id,stockCheck_id,remark,updater,update_time,actual_qty,expected_qty,product_id,id,status
 */
@Schema(description = "管理后台 - 库位盘点 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockCheckBinRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "11537")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "盘点结果单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5995")
    @ExcelProperty("盘点结果单ID")
    private Long stockCheckId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30522")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "预期库存，仓位可用库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预期库存")
    private Integer expectedQty;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "仓位ID", example = "")
    @ExcelProperty("仓位ID")
    private Long binId;

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

    @Schema(description = "产品", example = "")
    private WmsProductRespSimpleVO product;

    @Schema(description = "仓位", example = "")
    private WmsWarehouseBinRespVO bin;

    @Schema(description = "实际库存，实盘数量", example = "")
    @ExcelProperty("实际库存")
    private Integer actualQty;

    @Schema(description = "差异量", example = "")
    @ExcelProperty("差异量")
    private Integer deltaQty;

    @Schema(description = "备注", example = "")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "盘点状态", example = "")
    @ExcelProperty("盘点状态")
    private Integer status;

    @Schema(description = "创建人姓名", example = "张三")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "更新人姓名", example = "李四")
    @ExcelProperty("更新人姓名")
    private String updaterName;
}
