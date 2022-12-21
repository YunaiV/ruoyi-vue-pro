package cn.iocoder.yudao.module.system.controller.admin.sms.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - 短信日志 Response VO")
@Data
public class SmsLogRespVO {

    @Schema(description = "编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "短信渠道编号", required = true, example = "10")
    private Long channelId;

    @Schema(description = "短信渠道编码", required = true, example = "ALIYUN")
    private String channelCode;

    @Schema(description = "模板编号", required = true, example = "20")
    private Long templateId;

    @Schema(description = "模板编码", required = true, example = "test-01")
    private String templateCode;

    @Schema(description = "短信类型", required = true, example = "1")
    private Integer templateType;

    @Schema(description = "短信内容", required = true, example = "你好，你的验证码是 1024")
    private String templateContent;

    @Schema(description = "短信参数", required = true, example = "name,code")
    private Map<String, Object> templateParams;

    @Schema(description = "短信 API 的模板编号", required = true, example = "SMS_207945135")
    private String apiTemplateId;

    @Schema(description = "手机号", required = true, example = "15601691300")
    private String mobile;

    @Schema(description = "用户编号", example = "10")
    private Long userId;

    @Schema(description = "用户类型", example = "1")
    private Integer userType;

    @Schema(description = "发送状态", required = true, example = "1")
    private Integer sendStatus;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "发送结果的编码", example = "0")
    private Integer sendCode;

    @Schema(description = "发送结果的提示", example = "成功")
    private String sendMsg;

    @Schema(description = "短信 API 发送结果的编码", example = "SUCCESS")
    private String apiSendCode;

    @Schema(description = "短信 API 发送失败的提示", example = "成功")
    private String apiSendMsg;

    @Schema(description = "短信 API 发送返回的唯一请求 ID", example = "3837C6D3-B96F-428C-BBB2-86135D4B5B99")
    private String apiRequestId;

    @Schema(description = "短信 API 发送返回的序号", example = "62923244790")
    private String apiSerialNo;

    @Schema(description = "接收状态", required = true, example = "0")
    private Integer receiveStatus;

    @Schema(description = "接收时间")
    private LocalDateTime receiveTime;

    @Schema(description = "API 接收结果的编码", example = "DELIVRD")
    private String apiReceiveCode;

    @Schema(description = "API 接收结果的说明", example = "用户接收成功")
    private String apiReceiveMsg;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
