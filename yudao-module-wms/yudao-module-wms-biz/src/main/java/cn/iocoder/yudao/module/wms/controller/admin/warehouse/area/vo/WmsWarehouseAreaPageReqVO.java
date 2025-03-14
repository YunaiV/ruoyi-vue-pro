package cn.iocoder.yudao.module.wms.controller.admin.warehouse.area.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : stock_type,code,create_time,name,priority,partition_type,status,warehouse_id
 */
@Schema(description = "管理后台 - 库区分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsWarehouseAreaPageReqVO extends PageParam {

    @Schema(description = "代码")
    private String code;

    @Schema(description = "名称", example = "张三")
    private String name;

    @Schema(description = "归属的仓库ID", example = "11263")
    private Long warehouseId;

    @Schema(description = "分区类型 ; WarehouseAreaPartitionType : 1-标准品 , 2-不良品", example = "1")
    private Integer partitionType;

    @Schema(description = "状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用", example = "2")
    private Integer status;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "存货类型 ; WarehouseAreaStockType : 1-拣货 , 2-存储", example = "")
    private Integer stockType;
}
