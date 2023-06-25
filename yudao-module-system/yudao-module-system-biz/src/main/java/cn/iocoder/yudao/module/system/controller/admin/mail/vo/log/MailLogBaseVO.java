package cn.iocoder.yudao.module.system.controller.admin.mail.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 邮件日志 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class MailLogBaseVO {

    @Schema(description = "用户编号", example = "30883")
    private Long userId;

    @Schema(description = "用户类型 - 参见 UserTypeEnum 枚举", example = "2")
    private Byte userType;

    @Schema(description = "接收邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "76854@qq.com")
    @NotNull(message = "接收邮箱地址不能为空")
    private String toMail;

    @Schema(description = "邮箱账号编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18107")
    @NotNull(message = "邮箱账号编号不能为空")
    private Long accountId;

    @Schema(description = "发送邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "85757@qq.com")
    @NotNull(message = "发送邮箱地址不能为空")
    private String fromMail;

    @Schema(description = "模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5678")
    @NotNull(message = "模板编号不能为空")
    private Long templateId;

    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "test_01")
    @NotNull(message = "模板编码不能为空")
    private String templateCode;

    @Schema(description = "模版发送人名称", example = "李四")
    private String templateNickname;

    @Schema(description = "邮件标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试标题")
    @NotNull(message = "邮件标题不能为空")
    private String templateTitle;

    @Schema(description = "邮件内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试内容")
    @NotNull(message = "邮件内容不能为空")
    private String templateContent;

    @Schema(description = "邮件参数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "邮件参数不能为空")
    private Map<String, Object> templateParams;

    @Schema(description = "发送状态 - 参见 MailSendStatusEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "发送状态不能为空")
    private Byte sendStatus;

    @Schema(description = "发送时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime sendTime;

    @Schema(description = "发送返回的消息 ID", example = "28568")
    private String sendMessageId;

    @Schema(description = "发送异常")
    private String sendException;

}
