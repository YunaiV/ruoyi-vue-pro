package cn.iocoder.yudao.module.product.controller.admin.comment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.product.enums.comment.ProductCommentScoresEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 商品评价分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductCommentPageReqVO extends PageParam {

    @Schema(description = "评价人名称", example = "王二狗")
    private String userNickname;

    @Schema(description = "交易订单编号", example = "24428")
    private Long orderId;

    @Schema(description = "商品SPU编号", example = "29502")
    private Long spuId;

    @Schema(description = "商品SPU名称", example = "感冒药")
    private String spuName;

    @Schema(description = "评分星级 1-5 分", example = "5")
    @InEnum(ProductCommentScoresEnum.class)
    private Integer scores;

    @Schema(description = "商家是否回复", example = "true")
    private Boolean replyStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
