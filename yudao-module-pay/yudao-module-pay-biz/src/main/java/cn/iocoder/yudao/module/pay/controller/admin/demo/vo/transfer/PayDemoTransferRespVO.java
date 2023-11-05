package cn.iocoder.yudao.module.pay.controller.admin.demo.vo.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 示例业务转账订单 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PayDemoTransferRespVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "应用编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long appId;

    @Schema(description = "转账金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "22338")
    private Integer price;

    @Schema(description = "转账类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "收款人姓名",  example = "test")
    private String userName;

    @Schema(description = "支付宝登录号", example = "32167")
    private String alipayLogonId;

    @Schema(description = "微信 openId", example = "31139")
    private String openid;

    @Schema(description = "转账状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer transferStatus;

    @Schema(description = "转账订单编号", example = "23695")
    private Long payTransferId;

    @Schema(description = "转账支付成功渠道")
    private String payChannelCode;

    @Schema(description = "转账支付时间")
    private LocalDateTime transferTime;
}
