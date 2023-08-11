package cn.iocoder.yudao.module.member.controller.admin.point.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会员积分配置 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MemberPointConfigBaseVO {

    // TODO @xiaqing：参数校验注解

    @Schema(description = "积分抵扣开关", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean tradeDeductEnable;

    @Schema(description = "积分抵扣，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "13506")
    private Integer tradeDeductUnitPrice;

    @Schema(description = "积分抵扣最大值", requiredMode = Schema.RequiredMode.REQUIRED, example = "32428")
    private Integer tradeDeductMaxPrice;

    @Schema(description = "1 元赠送多少分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer tradeGivePoint;

}
