package cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 出库单详情分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsOutboundItemPageReqVO extends PageParam {

    @Schema(description = "入库单ID", example = "6602")
    private Long outboundId;

    @Schema(description = "标准产品ID", example = "20572")
    private Long productId;

    @Schema(description = "标准产品SKU")
    private String productSku;

    @Schema(description = "预期量")
    private Integer expectedQuantity;

    @Schema(description = "实际量")
    private Integer actualQuantity;

    @Schema(description = "来源详情ID", example = "11448")
    private Long sourceItemId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}