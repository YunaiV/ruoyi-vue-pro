package cn.iocoder.yudao.module.promotion.controller.admin.discount.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 限时折扣活动 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class DiscountActivityBaseVO {

    @ApiModelProperty(value = "活动标题", required = true, example = "一个标题")
    @NotNull(message = "活动标题不能为空")
    private String name;

    @ApiModelProperty(value = "开始时间", required = true)
    @NotNull(message = "开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date startTime;

    @ApiModelProperty(value = "结束时间", required = true)
    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date endTime;

    @ApiModelProperty(value = "备注", example = "我是备注")
    private String remark;

    @ApiModel("商品")
    @Data
    public static class Product {

        @ApiModelProperty(value = "商品 SPU 编号", required = true, example = "1")
        @NotNull(message = "商品 SPU 编号不能为空")
        private Long spuId;

        @ApiModelProperty(value = "商品 SKU 编号", required = true, example = "1")
        @NotNull(message = "商品 SKU 编号不能为空")
        private Long skuId;

        @ApiModelProperty(value = "折扣价格，单位：分", required = true, example = "1000")
        @NotNull(message = "折扣价格不能为空")
        @Min(value = 1, message = "折扣价格必须大于 0")
        private Integer discountPrice;

    }
}
