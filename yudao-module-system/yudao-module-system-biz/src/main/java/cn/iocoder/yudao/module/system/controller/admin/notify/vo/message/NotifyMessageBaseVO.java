package cn.iocoder.yudao.module.system.controller.admin.notify.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 站内信消息 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class NotifyMessageBaseVO {

    @Schema(description = "用户编号", required = true, example = "25025")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "用户类型 - 参见 UserTypeEnum 枚举", required = true, example = "1")
    @NotNull(message = "用户类型不能为空")
    private Byte userType;

    @Schema(description = "模版编号", required = true, example = "13013")
    @NotNull(message = "模版编号不能为空")
    private Long templateId;

    @Schema(description = "模板编码", required = true, example = "test_01")
    @NotNull(message = "模板编码不能为空")
    private String templateCode;

    @Schema(description = "模版发送人名称", required = true, example = "芋艿")
    @NotNull(message = "模版发送人名称不能为空")
    private String templateNickname;

    @Schema(description = "模版内容", required = true, example = "测试内容")
    @NotNull(message = "模版内容不能为空")
    private String templateContent;

    @Schema(description = "模版类型", required = true, example = "2")
    @NotNull(message = "模版类型不能为空")
    private Integer templateType;

    @Schema(description = "模版参数", required = true)
    @NotNull(message = "模版参数不能为空")
    private Map<String, Object> templateParams;

    @Schema(description = "是否已读", required = true, example = "true")
    @NotNull(message = "是否已读不能为空")
    private Boolean readStatus;

    @Schema(description = "阅读时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime readTime;

}
