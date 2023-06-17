package cn.iocoder.yudao.module.point.controller.admin.pointconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 积分设置 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PointConfigBaseVO {

    @Schema(description = "1 开启积分抵扣 0 关闭积分抵扣", example = "1")
    private Integer tradeDeductEnable;

    @Schema(description = "积分抵扣，抵扣最低为分 以0.01表示 1积分抵扣0.01元(单位：元)", example = "13506")
    private BigDecimal tradeDeductUnitPrice;

    @Schema(description = "积分抵扣最大值", example = "32428")
    private Long tradeDeductMaxPrice;

    @Schema(description = "1元赠送多少分")
    private Long tradeGivePoint;

}
