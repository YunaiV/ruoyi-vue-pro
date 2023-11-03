package cn.iocoder.yudao.module.pay.controller.admin.transfer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Schema(description = "管理后台 - 转账单提交 Request VO")
@Data
public class PayTransferSubmitReqVO {

    @Schema(description = "转账单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "转账单编号不能为空")
    private Long id;

    @Schema(description = "转账渠道", requiredMode = Schema.RequiredMode.REQUIRED, example = "alipay_transfer")
    @NotEmpty(message = "转账渠道不能为空")
    private String channelCode;

    @Schema(description = "转账渠道的额外参数")
    private Map<String, String> channelExtras;

}
