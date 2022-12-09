package cn.iocoder.yudao.module.system.controller.admin.sms.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(title = "管理后台 - 短信日志 Response VO")
@Data
public class SmsLogRespVO {

    @Schema(title = "编号", required = true, example = "1024")
    private Long id;

    @Schema(title = "短信渠道编号", required = true, example = "10")
    private Long channelId;

    @Schema(title = "短信渠道编码", required = true, example = "ALIYUN")
    private String channelCode;

    @Schema(title = "模板编号", required = true, example = "20")
    private Long templateId;

    @Schema(title = "模板编码", required = true, example = "test-01")
    private String templateCode;

    @Schema(title = "短信类型", required = true, example = "1")
    private Integer templateType;

    @Schema(title = "短信内容", required = true, example = "你好，你的验证码是 1024")
    private String templateContent;

    @Schema(title = "短信参数", required = true, example = "name,code")
    private Map<String, Object> templateParams;

    @Schema(title = "短信 API 的模板编号", required = true, example = "SMS_207945135")
    private String apiTemplateId;

    @Schema(title = "手机号", required = true, example = "15601691300")
    private String mobile;

    @Schema(title = "用户编号", example = "10")
    private Long userId;

    @Schema(title = "用户类型", example = "1")
    private Integer userType;

    @Schema(title = "发送状态", required = true, example = "1")
    private Integer sendStatus;

    @Schema(title = "发送时间")
    private LocalDateTime sendTime;

    @Schema(title = "发送结果的编码", example = "0")
    private Integer sendCode;

    @Schema(title = "发送结果的提示", example = "成功")
    private String sendMsg;

    @Schema(title = "短信 API 发送结果的编码", example = "SUCCESS")
    private String apiSendCode;

    @Schema(title = "短信 API 发送失败的提示", example = "成功")
    private String apiSendMsg;

    @Schema(title = "短信 API 发送返回的唯一请求 ID", example = "3837C6D3-B96F-428C-BBB2-86135D4B5B99")
    private String apiRequestId;

    @Schema(title = "短信 API 发送返回的序号", example = "62923244790")
    private String apiSerialNo;

    @Schema(title = "接收状态", required = true, example = "0")
    private Integer receiveStatus;

    @Schema(title = "接收时间")
    private LocalDateTime receiveTime;

    @Schema(title = "API 接收结果的编码", example = "DELIVRD")
    private String apiReceiveCode;

    @Schema(title = "API 接收结果的说明", example = "用户接收成功")
    private String apiReceiveMsg;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

}
