package cn.iocoder.yudao.module.promotion.controller.admin.point.vo.product;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 积分商城商品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PointProductPageReqVO extends PageParam {

    @Schema(description = "积分商城活动 id", example = "29388")
    private Long activityId;

    @Schema(description = "商品 SPU 编号", example = "8112")
    private Long spuId;

    @Schema(description = "商品 SKU 编号", example = "2736")
    private Long skuId;

    @Schema(description = "可兑换数量", example = "3926")
    private Integer maxCount;

    @Schema(description = "兑换积分")
    private Integer point;

    @Schema(description = "兑换金额，单位：分", example = "15860")
    private Integer price;

    @Schema(description = "兑换类型", example = "2")
    private Integer type;

    @Schema(description = "积分商城商品状态", example = "2")
    private Integer activityStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}