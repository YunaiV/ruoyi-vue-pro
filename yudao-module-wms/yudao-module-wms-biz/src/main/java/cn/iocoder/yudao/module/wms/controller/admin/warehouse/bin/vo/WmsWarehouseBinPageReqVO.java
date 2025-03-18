package cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : zone_id,code,create_time,picking_order,name,status,warehouse_id
 */
@Schema(description = "管理后台 - 库位分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsWarehouseBinPageReqVO extends PageParam {

    @Schema(description = "代码")
    private String code;

    @Schema(description = "名称", example = "王五")
    private String name;

    @Schema(description = "归属的仓库ID", example = "14159")
    private Long warehouseId;

    @Schema(description = "库区ID", example = "29995")
    private Long zoneId;

    @Schema(description = "拣货顺序")
    private Integer pickingOrder;

    @Schema(description = "状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
