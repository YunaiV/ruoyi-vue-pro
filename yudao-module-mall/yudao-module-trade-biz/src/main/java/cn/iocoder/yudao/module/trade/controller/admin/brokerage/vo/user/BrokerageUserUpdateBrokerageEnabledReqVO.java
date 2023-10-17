package cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 分销用户 - 修改推广员 Request VO")
@Data
@ToString(callSuper = true)
public class BrokerageUserUpdateBrokerageEnabledReqVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20019")
    @NotNull(message = "用户编号不能为空")
    private Long id;

    @Schema(description = "推广资格", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "推广资格不能为空")
    private Boolean enabled;

}
