package cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 库存盘点产品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInventoryProductPageReqVO extends PageParam {

    @Schema(description = "盘点结果单ID", example = "7233")
    private Long inventoryId;

    @Schema(description = "产品ID", example = "25033")
    private Long productId;

    @Schema(description = "预期库存，产品总可用库存")
    private Integer expectedQty;

    @Schema(description = "实际库存，实盘数量")
    private Integer actualQty;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}