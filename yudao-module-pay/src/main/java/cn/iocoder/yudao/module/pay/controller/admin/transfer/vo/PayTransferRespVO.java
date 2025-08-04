package cn.iocoder.yudao.module.pay.controller.admin.transfer.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.excel.core.convert.MoneyConvert;
import cn.iocoder.yudao.module.pay.enums.DictTypeConstants;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - 转账单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PayTransferRespVO {

    @ExcelProperty("转账单编号")
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2931")
    private Long id;

    @ExcelProperty("转账单号")
    @Schema(description = "转账单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String no;

    @Schema(description = "应用编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12831")
    private Long appId;

    @ExcelProperty("应用名称")
    @Schema(description = "应用名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String appName;

    @Schema(description = "转账渠道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24833")
    private Long channelId;

    @ExcelProperty(value = "转账渠道", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.CHANNEL_CODE)
    @Schema(description = "转账渠道编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channelCode;

    @ExcelProperty("商户转账单编号")
    @Schema(description = "商户转账单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17481")
    private String merchantTransferId;

    @ExcelProperty(value = "转账状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.TRANSFER_STATUS)
    @Schema(description = "转账状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

    @ExcelProperty("转账成功时间")
    @Schema(description = "转账成功时间")
    private LocalDateTime successTime;

    @Schema(description = "转账金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "964")
    @ExcelProperty(value = "转账金额", converter = MoneyConvert.class)
    private Integer price;

    @ExcelProperty("转账标题")
    @Schema(description = "转账标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "冲冲冲！")
    private String subject;

    @Schema(description = "收款人姓名", example = "王五")
    @ExcelProperty("收款人姓名")
    private String userName;

    @Schema(description = "收款人账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26589")
    @ExcelProperty("收款人账号")
    private String userAccount;

    @Schema(description = "异步通知商户地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    private String notifyUrl;

    @ExcelProperty("用户 IP")
    @Schema(description = "用户 IP", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userIp;

    @Schema(description = "渠道的额外参数")
    private Map<String, String> channelExtras;

    @Schema(description = "渠道转账单号")
    @ExcelProperty("渠道转账单号")
    private String channelTransferNo;

    @Schema(description = "调用渠道的错误码")
    private String channelErrorCode;

    @ExcelProperty("渠道错误提示")
    @Schema(description = "调用渠道的错误提示")
    private String channelErrorMsg;

    @Schema(description = "渠道的同步/异步通知的内容")
    private String channelNotifyData;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
