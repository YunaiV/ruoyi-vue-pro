package cn.iocoder.yudao.module.wms.controller.admin.pickup.vo;

import cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo.WmsPickupItemRespVO;
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
 * @table-fields : tenant_id,creator,update_time,code,create_time,upstream_bill_id,id,upstream_bill_code,warehouse_id,upstream_bill_type,updater
 */
@Schema(description = "管理后台 - 拣货单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsPickupRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "8396")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22380")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人姓名", example = "张三")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "更新人姓名", example = "李四")
    @ExcelProperty("更新人姓名")
    private String updaterName;

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

    @Schema(description = "详情清单", example = "")
    @ExcelProperty("详情清单")
    private List<WmsPickupItemRespVO> itemList;

    @Schema(description = "仓库", example = "")
    private WmsWarehouseSimpleRespVO warehouse;

    @Schema(description = "单据号", example = "")
    @ExcelProperty("单据号")
    private String code;

    @Schema(description = "来源单据ID", example = "")
    @ExcelProperty("来源单据ID")
    private Long upstreamBillId;

    @Schema(description = "来源单据号", example = "")
    @ExcelProperty("来源单据号")
    private String upstreamBillCode;

    @Schema(description = "来源单据类型", example = "")
    @ExcelProperty("来源单据类型")
    private Integer upstreamBillType;
}
