package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(title = "管理后台 - 应用更新状态 Request VO")
@Data
public class PayAppUpdateStatusReqVO {

    @Schema(title = "商户编号", required = true, example = "1024")
    @NotNull(message = "商户编号不能为空")
    private Long id;

    @Schema(title = "状态", required = true, example = "1", description = "见 SysCommonStatusEnum 枚举")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
