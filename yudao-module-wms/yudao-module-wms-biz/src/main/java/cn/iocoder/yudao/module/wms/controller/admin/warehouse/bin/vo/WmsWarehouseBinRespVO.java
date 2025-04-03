package cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo;

import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone.vo.WmsWarehouseZoneSimpleRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,zone_id,creator,update_time,code,create_time,picking_order,name,id,status,updater,warehouse_id
 */
@Schema(description = "管理后台 - 库位 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsWarehouseBinRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "23735")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("代码")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("名称")
    private String name;

    @Schema(description = "归属的仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14159")
    @ExcelProperty("归属的仓库ID")
    private Long warehouseId;

    @Schema(description = "库区ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29995")
    @ExcelProperty("库区ID")
    private Long zoneId;

    @Schema(description = "拣货顺序")
    @ExcelProperty("拣货顺序")
    private Integer pickingOrder;

    @Schema(description = "状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态")
    private Integer status;

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

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "仓库")
    private WmsWarehouseSimpleRespVO warehouse;

    @Schema(description = "仓库")
    private WmsWarehouseZoneSimpleRespVO zone;
}
