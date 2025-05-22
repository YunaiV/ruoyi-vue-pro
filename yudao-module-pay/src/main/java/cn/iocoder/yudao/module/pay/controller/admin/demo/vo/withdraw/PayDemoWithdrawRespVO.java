package cn.iocoder.yudao.module.pay.controller.admin.demo.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 示例转账单创建 Request VO")
@Data
public class PayDemoWithdrawRespVO {

    @Schema(description = "转账单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "提现标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "吃饭报销")
    private String subject;

    @Schema(description = "提现金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "22338")
    private Integer price;

    @Schema(description = "收款人姓名",  example = "test")
    private String userName;

    @Schema(description = "收款人账号", example = "32167")
    private String userAccount;

    @Schema(description = "提现类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "提现状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

    // ========== 转账相关字段 ==========

    @Schema(description = "转账单编号", example = "23695")
    private Long payTransferId;

    @Schema(description = "转账渠道", example = "wx_lite")
    private String transferChannelCode;

    @Schema(description = "转账成功时间")
    private LocalDateTime transferTime;

    @Schema(description = "转账失败原因", example = "IP 不正确")
    private String transferErrorMsg;

}
