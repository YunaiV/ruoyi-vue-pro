package cn.iocoder.yudao.module.bpm.controller.admin.oa.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(title = "管理后台 - 请假申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmOALeavePageReqVO extends PageParam {

    @Schema(title = "状态", example = "1", description = "参见 bpm_process_instance_result 枚举")
    private Integer result;

    @Schema(title = "请假类型", example = "1", description = "参见 bpm_oa_type")
    private Integer type;

    @Schema(title = "原因", example = "阅读芋道源码", description = "模糊匹配")
    private String reason;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "申请时间")
    private LocalDateTime[] createTime;

}
