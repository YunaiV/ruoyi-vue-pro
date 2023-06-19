package cn.iocoder.yudao.module.member.controller.admin.point.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员积分配置 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MemberPointConfigBaseVO {

    @Schema(description = "积分抵扣开关", required = true, example = "true")
    private Boolean tradeDeductEnable;

    @Schema(description = "积分抵扣，单位：分", example = "13506")
    private BigDecimal tradeDeductUnitPrice;

    @Schema(description = "积分抵扣最大值", example = "32428")
    private Long tradeDeductMaxPrice;

    @Schema(description = "1 元赠送多少分")
    private Long tradeGivePoint;

}
