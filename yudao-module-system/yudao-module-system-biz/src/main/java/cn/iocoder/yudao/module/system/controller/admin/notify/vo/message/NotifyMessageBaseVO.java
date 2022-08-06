package cn.iocoder.yudao.module.system.controller.admin.notify.vo.message;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 站内信 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class NotifyMessageBaseVO {

    @ApiModelProperty(value = "用户编号", required = true)
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @ApiModelProperty(value = "用户类型", required = true)
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "内容", required = true)
    @NotNull(message = "内容不能为空")
    private String content;

    @ApiModelProperty(value = "是否已读 0-未读  1-已读")
    private Boolean readStatus;

    @ApiModelProperty(value = "阅读时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date readTime;

}
