package cn.iocoder.yudao.module.system.controller.admin.notify.vo.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 站内信消息 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class NotifyMessageBaseVO {

    @ApiModelProperty(value = "用户编号", required = true, example = "25025")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @ApiModelProperty(value = "用户类型", required = true, example = "1", notes = "参见 UserTypeEnum 枚举")
    @NotNull(message = "用户类型不能为空")
    private Byte userType;

    @ApiModelProperty(value = "模版编号", required = true, example = "13013")
    @NotNull(message = "模版编号不能为空")
    private Long templateId;

    @ApiModelProperty(value = "模板编码", required = true, example = "test_01")
    @NotNull(message = "模板编码不能为空")
    private String templateCode;

    @ApiModelProperty(value = "模版发送人名称", required = true, example = "芋艿")
    @NotNull(message = "模版发送人名称不能为空")
    private String templateNickname;

    @ApiModelProperty(value = "模版内容", required = true, example = "测试内容")
    @NotNull(message = "模版内容不能为空")
    private String templateContent;

    @ApiModelProperty(value = "模版类型", required = true, example = "2")
    @NotNull(message = "模版类型不能为空")
    private Integer templateType;

    @ApiModelProperty(value = "模版参数", required = true)
    @NotNull(message = "模版参数不能为空")
    private Map<String, Object> templateParams;

    @ApiModelProperty(value = "是否已读", required = true, example = "true")
    @NotNull(message = "是否已读不能为空")
    private Boolean readStatus;

    @ApiModelProperty(value = "阅读时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime readTime;

}
