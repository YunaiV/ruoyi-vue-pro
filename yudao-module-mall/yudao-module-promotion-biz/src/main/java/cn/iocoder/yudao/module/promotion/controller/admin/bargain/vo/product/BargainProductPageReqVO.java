package cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.product;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 砍价商品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BargainProductPageReqVO extends PageParam {

    @Schema(description = "砍价活动编号", example = "6829")
    private Long activityId;

    @Schema(description = "商品 SPU 编号", example = "18731")
    private Long spuId;

    @Schema(description = "商品 SKU 编号", example = "31675")
    private Long skuId;

    @Schema(description = "砍价商品状态", example = "2")
    private Integer activityStatus;

    @Schema(description = "活动开始时间点")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] activityStartTime;

    @Schema(description = "活动结束时间点")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] activityEndTime;

    @Schema(description = "砍价价格，单位分", example = "27682")
    private Integer activePrice;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
