package cn.iocoder.yudao.module.system.controller.admin.mail.vo.log;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 邮箱日志返回 Request VO")
@Data
public class MailLogRespVO {

    @ApiModelProperty(value = "邮箱" , required = false , example = "yudaoyuanma@123.com")
    private String from;

    @ApiModelProperty(value = "模版编号" , required = false , example = "templeId")
    private String templateId;

    @ApiModelProperty(value = "模版code" , required = false , example = "templeCode")
    private String templateCode;

    @ApiModelProperty(value = "标题" , required = false , example = "芋道源码")
    private String title;

    @ApiModelProperty(value = "内容" , required = false , example = "遇到源码")
    private String content;

    @ApiModelProperty(value = "收件人" , required = false , example = "yudaoyuanma@456.com")
    private String to;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "发送时间" , required = false , example = "2022-03-26 03:45:20")
    private Timestamp sendTime;

    @ApiModelProperty(value = "发送状态" , required = false , example = "1")
    private Integer sendStatus;

    @ApiModelProperty(value = "发送结果" , required = false , example = "yudaoyuanma@123.com")
    private String sendResult;

}
