package cn.iocoder.yudao.module.wms.controller.admin.inventory.result.item.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 库存盘点结果详情分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInventoryResultItemPageReqVO extends PageParam {

    @Schema(description = "盘点结果单ID", example = "2645")
    private Long resultId;

    @Schema(description = "产品ID", example = "20359")
    private String productId;

    @Schema(description = "预期库存")
    private Integer expectedQty;

    @Schema(description = "实际库存")
    private Integer actualQty;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}