package cn.iocoder.yudao.module.oms.controller.admin.order.item.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - OMS订单项分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OmsOrderItemPageReqVO extends PageParam {

    @Schema(description = "销售订单id", example = "30317")
    private Long orderId;

    @Schema(description = "店铺产品编码")
    private String shopProductCode;

    @Schema(description = "店铺产品数量")
    private Integer qty;

    @Schema(description = "单价", example = "1098")
    private BigDecimal price;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}