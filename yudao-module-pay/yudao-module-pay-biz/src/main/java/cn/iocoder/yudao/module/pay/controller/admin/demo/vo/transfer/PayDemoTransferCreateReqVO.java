package cn.iocoder.yudao.module.pay.controller.admin.demo.vo.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 示例转账单创建 Request VO")
@Data
public class PayDemoTransferCreateReqVO {

    @Schema(description = "转账渠道", requiredMode = Schema.RequiredMode.REQUIRED, example = "wx_lite")
    @NotEmpty(message = "转账渠道不能为空")
    private String channelCode;

    @Schema(description = "转账标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿是一种菜")
    @NotEmpty(message = "转账标题不能为空")
    private String subject;

    @Schema(description = "转账金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "转账金额不能为空")
    @Min(value = 1, message = "转账金额必须大于零")
    private Integer price;

    @Schema(description = "收款人账号", requiredMode= Schema.RequiredMode.REQUIRED, example = "test1")
    @NotBlank(message = "收款人账号不能为空")
    private String userAccount;

    @Schema(description = "收款人姓名", example = "test1")
    @NotBlank(message = "收款人姓名不能为空")
    private String userName;

}
