package cn.iocoder.yudao.module.system.controller.admin.mail.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 邮件日志 Response VO")
@Data
public class MailLogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31020")
    private Long id;

    @Schema(description = "用户编号", example = "30883")
    private Long userId;

    @Schema(description = "用户类型，参见 UserTypeEnum 枚举", example = "2")
    private Byte userType;

    @Schema(description = "接收邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "76854@qq.com")
    private String toMail;

    @Schema(description = "邮箱账号编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18107")
    private Long accountId;

    @Schema(description = "发送邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "85757@qq.com")
    private String fromMail;

    @Schema(description = "模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5678")
    private Long templateId;

    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "test_01")
    private String templateCode;

    @Schema(description = "模版发送人名称", example = "李四")
    private String templateNickname;

    @Schema(description = "邮件标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试标题")
    private String templateTitle;

    @Schema(description = "邮件内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试内容")
    private String templateContent;

    @Schema(description = "邮件参数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> templateParams;

    @Schema(description = "发送状态，参见 MailSendStatusEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Byte sendStatus;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "发送返回的消息 ID", example = "28568")
    private String sendMessageId;

    @Schema(description = "发送异常")
    private String sendException;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
