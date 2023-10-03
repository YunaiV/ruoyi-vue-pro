package cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 分销用户 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BrokerageUserRespVO extends BrokerageUserBaseVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20019")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 用户信息 ==========

    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xxx.png")
    private String avatar;
    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String nickname;

    // ========== 推广信息 ========== 注意：是包括 1 + 2 级的数据

    @Schema(description = "推广用户数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20019")
    private Integer brokerageUserCount;
    @Schema(description = "推广订单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20019")
    private Integer brokerageOrderCount;
    @Schema(description = "推广订单金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "20019")
    private Integer brokerageOrderPrice;

    // ========== 提现信息 ==========

    @Schema(description = "已提现金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "20019")
    private Integer withdrawPrice;
    @Schema(description = "已提现次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20019")
    private Integer withdrawCount;

}
