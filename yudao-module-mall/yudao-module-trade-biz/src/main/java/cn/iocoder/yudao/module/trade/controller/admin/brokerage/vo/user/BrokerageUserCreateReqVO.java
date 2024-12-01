package cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 分销用户创建 Request VO")
@Data
public class BrokerageUserCreateReqVO {

    @Schema(description = "分销用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "分销用户编号不能为空")
    private Long userId;

    @Schema(description = "推广员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4587")
    private Long bindUserId;

    @Schema(description = "推广资格", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "推广资格不能为空")
    private Boolean brokerageEnabled;

    @Schema(description = "可用佣金", requiredMode = Schema.RequiredMode.REQUIRED, example = "11089")
    @NotNull(message = "可用佣金不能为空")
    private Integer price;

    @Schema(description = "冻结佣金", requiredMode = Schema.RequiredMode.REQUIRED, example = "30916")
    @NotNull(message = "冻结佣金不能为空")
    private Integer frozenPrice;

}
