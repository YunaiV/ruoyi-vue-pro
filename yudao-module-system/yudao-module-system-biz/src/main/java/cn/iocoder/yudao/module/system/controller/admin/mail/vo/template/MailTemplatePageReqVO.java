package cn.iocoder.yudao.module.system.controller.admin.mail.vo.template;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 邮件模版分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MailTemplatePageReqVO extends PageParam {

    @Schema(description = "状态 - 参见 CommonStatusEnum 枚举", example = "1")
    private Integer status;

    @Schema(description = "标识 - 模糊匹配", example = "code_1024")
    private String code;

    @Schema(description = "名称 - 模糊匹配", example = "芋头")
    private String name;

    @Schema(description = "账号编号", example = "2048")
    private Long accountId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
