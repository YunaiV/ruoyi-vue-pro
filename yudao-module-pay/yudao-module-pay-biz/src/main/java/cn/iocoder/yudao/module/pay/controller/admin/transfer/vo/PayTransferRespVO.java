package cn.iocoder.yudao.module.pay.controller.admin.transfer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - 转账单 Response VO")
@Data
public class PayTransferRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2931")
    private Long id;

    @Schema(description = "转账单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String no;

    @Schema(description = "应用编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12831")
    private Long appId;

    @Schema(description = "转账渠道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24833")
    private Long channelId;

    @Schema(description = "转账渠道编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channelCode;

    @Schema(description = "商户转账单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17481")
    private String merchantTransferId;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer type;

    @Schema(description = "转账状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

    @Schema(description = "转账成功时间")
    private LocalDateTime successTime;

    @Schema(description = "转账金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "964")
    private Integer price;

    @Schema(description = "转账标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String subject;

    @Schema(description = "收款人姓名", example = "王五")
    private String userName;

    @Schema(description = "支付宝登录号", example = "29245")
    private String alipayLogonId;

    @Schema(description = "微信 openId", example = "26589")
    private String openid;

    @Schema(description = "异步通知商户地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    private String notifyUrl;

    @Schema(description = "用户 IP", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userIp;

    @Schema(description = "渠道的额外参数")
    private Map<String, String> channelExtras;

    @Schema(description = "渠道转账单号")
    private String channelTransferNo;

    @Schema(description = "调用渠道的错误码")
    private String channelErrorCode;

    @Schema(description = "调用渠道的错误提示")
    private String channelErrorMsg;

    @Schema(description = "渠道的同步/异步通知的内容")
    private String channelNotifyData;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
