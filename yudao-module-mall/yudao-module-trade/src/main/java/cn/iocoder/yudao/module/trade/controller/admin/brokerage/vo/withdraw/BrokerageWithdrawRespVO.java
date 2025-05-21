package cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 佣金提现 Response VO")
@Data
public class BrokerageWithdrawRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7161")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11436")
    private Long userId;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String userNickname;

    @Schema(description = "提现金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "18781")
    private Integer price;

    @Schema(description = "提现手续费", requiredMode = Schema.RequiredMode.REQUIRED, example = "11417")
    private Integer feePrice;

    @Schema(description = "当前总佣金", requiredMode = Schema.RequiredMode.REQUIRED, example = "18576")
    private Integer totalPrice;

    @Schema(description = "提现类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "真实姓名", example = "赵六")
    private String userName;

    @Schema(description = "收款账号", example = "88677912132")
    private String userAccount;

    @Schema(description = "银行名称", example = "1")
    private String bankName;

    @Schema(description = "开户地址", example = "海淀支行")
    private String bankAddress;

    @Schema(description = "收款码", example = "https://www.iocoder.cn")
    private String qrCodeUrl;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "审核驳回原因", example = "不对")
    private String auditReason;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "转账单编号", example = "1024")
    private Long payTransferId;

    @Schema(description = "转账错误提示", example = "余额不足")
    private String transferErrorMsg;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
