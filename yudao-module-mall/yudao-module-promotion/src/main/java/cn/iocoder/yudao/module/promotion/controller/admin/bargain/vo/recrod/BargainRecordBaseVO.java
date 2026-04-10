package cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.recrod;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 砍价记录 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class BargainRecordBaseVO {

    @Schema(description = "砍价活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "22690")
    @NotNull(message = "砍价活动名称不能为空")
    private Long activityId;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9430")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23622")
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29950")
    @NotNull(message = "商品 SKU 编号不能为空")
    private Long skuId;

    @Schema(description = "砍价起始价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "31160")
    @NotNull(message = "砍价起始价格，单位：分不能为空")
    private Integer bargainFirstPrice;

    @Schema(description = "当前砍价，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "22743")
    @NotNull(message = "当前砍价，单位：分不能为空")
    private Integer bargainPrice;

    @Schema(description = "砍价状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "砍价状态不能为空")
    private Integer status;

    @Schema(description = "订单编号", example = "27845")
    private Long orderId;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

}
