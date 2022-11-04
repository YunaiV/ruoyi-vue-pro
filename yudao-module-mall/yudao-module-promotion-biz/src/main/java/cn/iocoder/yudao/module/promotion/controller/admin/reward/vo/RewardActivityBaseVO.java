package cn.iocoder.yudao.module.promotion.controller.admin.reward.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 满减送活动 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class RewardActivityBaseVO {

    @ApiModelProperty(value = "活动标题", required = true, example = "满啦满啦")
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

    @ApiModelProperty(value = "备注", example = "biubiubiu")
    private String remark;

    @ApiModelProperty(value = "条件类型", required = true, example = "1")
    @NotNull(message = "条件类型不能为空")
    private Integer conditionType;

    @ApiModelProperty(value = "商品范围", required = true, example = "1")
    @NotNull(message = "商品范围不能为空")
    private Integer productScope;

    @ApiModelProperty(value = "商品 SPU 编号的数组", example = "1,2,3")
    private List<Long> productSpuIds;

    /**
     * 优惠规则的数组
     */
    private List<Rule> rules;

    @ApiModel("优惠规则")
    @Data
    public static class Rule {

        @ApiModelProperty(value = "优惠门槛", required = true, example = "100", notes = "1. 满 N 元，单位：分; 2. 满 N 件")
        private Integer limit;

        @ApiModelProperty(value = "优惠价格", required = true, example = "100", notes = "单位：分")
        private Integer discountPrice;

        @ApiModelProperty(value = "是否包邮", required = true, example = "true")
        private Boolean freeDelivery;

        @ApiModelProperty(value = "赠送的积分", required = true, example = "100")
        private Integer point;

        @ApiModelProperty(value = "赠送的优惠劵编号的数组", example = "1,2,3")
        private List<Long> couponIds;

        @ApiModelProperty(value = "赠送的优惠卷数量的数组", example = "1,2,3")
        private List<Integer> couponCounts;

    }

}
