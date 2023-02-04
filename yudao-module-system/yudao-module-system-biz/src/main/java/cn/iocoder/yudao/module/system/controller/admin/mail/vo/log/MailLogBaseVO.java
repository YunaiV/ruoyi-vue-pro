package cn.iocoder.yudao.module.system.controller.admin.mail.vo.log;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import io.swagger.annotations.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 邮件日志 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class MailLogBaseVO {

    @ApiModelProperty(value = "用户编号", example = "30883")
    private Long userId;

    @ApiModelProperty(value = "用户类型", example = "2", notes = "参见 UserTypeEnum 枚举")
    private Byte userType;

    @ApiModelProperty(value = "接收邮箱地址", required = true, example = "76854@qq.com")
    @NotNull(message = "接收邮箱地址不能为空")
    private String toMail;

    @ApiModelProperty(value = "邮箱账号编号", required = true, example = "18107")
    @NotNull(message = "邮箱账号编号不能为空")
    private Long accountId;

    @ApiModelProperty(value = "发送邮箱地址", required = true, example = "85757@qq.com")
    @NotNull(message = "发送邮箱地址不能为空")
    private String fromMail;

    @ApiModelProperty(value = "模板编号", required = true, example = "5678")
    @NotNull(message = "模板编号不能为空")
    private Long templateId;

    @ApiModelProperty(value = "模板编码", required = true, example = "test_01")
    @NotNull(message = "模板编码不能为空")
    private String templateCode;

    @ApiModelProperty(value = "模版发送人名称", example = "李四")
    private String templateNickname;

    @ApiModelProperty(value = "邮件标题", required = true, example = "测试标题")
    @NotNull(message = "邮件标题不能为空")
    private String templateTitle;

    @ApiModelProperty(value = "邮件内容", required = true, example = "测试内容")
    @NotNull(message = "邮件内容不能为空")
    private String templateContent;

    @ApiModelProperty(value = "邮件参数", required = true)
    @NotNull(message = "邮件参数不能为空")
    private Map<String, Object> templateParams;

    @ApiModelProperty(value = "发送状态", required = true, example = "1", notes = "参见 MailSendStatusEnum 枚举")
    @NotNull(message = "发送状态不能为空")
    private Byte sendStatus;

    @ApiModelProperty(value = "发送时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime sendTime;

    @ApiModelProperty(value = "发送返回的消息 ID", example = "28568")
    private String sendMessageId;

    @ApiModelProperty(value = "发送异常")
    private String sendException;

}
