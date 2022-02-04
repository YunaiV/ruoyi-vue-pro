package cn.iocoder.yudao.module.system.controller.admin.sms.vo.log;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@ApiModel("管理后台 - 短信日志 Response VO")
@Data
public class SmsLogRespVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "短信渠道编号", required = true, example = "10")
    private Long channelId;

    @ApiModelProperty(value = "短信渠道编码", required = true, example = "ALIYUN")
    private String channelCode;

    @ApiModelProperty(value = "模板编号", required = true, example = "20")
    private Long templateId;

    @ApiModelProperty(value = "模板编码", required = true, example = "test-01")
    private String templateCode;

    @ApiModelProperty(value = "短信类型", required = true, example = "1")
    private Integer templateType;

    @ApiModelProperty(value = "短信内容", required = true, example = "你好，你的验证码是 1024")
    private String templateContent;

    @ApiModelProperty(value = "短信参数", required = true, example = "name,code")
    private Map<String, Object> templateParams;

    @ApiModelProperty(value = "短信 API 的模板编号", required = true, example = "SMS_207945135")
    private String apiTemplateId;

    @ApiModelProperty(value = "手机号", required = true, example = "15601691300")
    private String mobile;

    @ApiModelProperty(value = "用户编号", example = "10")
    private Long userId;

    @ApiModelProperty(value = "用户类型", example = "1")
    private Integer userType;

    @ApiModelProperty(value = "发送状态", required = true, example = "1")
    private Integer sendStatus;

    @ApiModelProperty(value = "发送时间")
    private Date sendTime;

    @ApiModelProperty(value = "发送结果的编码", example = "0")
    private Integer sendCode;

    @ApiModelProperty(value = "发送结果的提示", example = "成功")
    private String sendMsg;

    @ApiModelProperty(value = "短信 API 发送结果的编码", example = "SUCCESS")
    private String apiSendCode;

    @ApiModelProperty(value = "短信 API 发送失败的提示", example = "成功")
    private String apiSendMsg;

    @ApiModelProperty(value = "短信 API 发送返回的唯一请求 ID", example = "3837C6D3-B96F-428C-BBB2-86135D4B5B99")
    private String apiRequestId;

    @ApiModelProperty(value = "短信 API 发送返回的序号", example = "62923244790")
    private String apiSerialNo;

    @ApiModelProperty(value = "接收状态", required = true, example = "0")
    private Integer receiveStatus;

    @ApiModelProperty(value = "接收时间")
    private Date receiveTime;

    @ApiModelProperty(value = "API 接收结果的编码", example = "DELIVRD")
    private String apiReceiveCode;

    @ApiModelProperty(value = "API 接收结果的说明", example = "用户接收成功")
    private String apiReceiveMsg;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
