package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - 服装设计流水线任务分页 Request VO
 *
 * @author deepay
 */
@Schema(description = "管理后台 - 服装设计流水线任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AiFashionTaskPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "任务状态（10进行中 20已完成 30已失败）", example = "20")
    private Integer status;

    @Schema(description = "提示词（模糊匹配）", example = "春夏")
    private String prompt;

    @Schema(description = "创建时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
