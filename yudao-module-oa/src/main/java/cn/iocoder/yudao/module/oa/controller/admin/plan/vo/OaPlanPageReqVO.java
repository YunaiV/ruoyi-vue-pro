package cn.iocoder.yudao.module.oa.controller.admin.plan.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.plan.OaPlanStatusEnum;
import cn.iocoder.yudao.module.oa.enums.plan.OaPlanTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - OA 工作计划分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaPlanPageReqVO extends PageParam {

    @Schema(description = "标题", example = "本周工作计划")
    private String title;

    @Schema(description = "标签", example = "重点")
    private String label;

    @Schema(description = "计划类型", example = "1")
    @InEnum(value = OaPlanTypeEnum.class, message = "计划类型必须是 {value}")
    private Integer type;

    @Schema(description = "计划状态", example = "1")
    @InEnum(value = OaPlanStatusEnum.class, message = "计划状态必须是 {value}")
    private Integer status;

    @Schema(description = "创建时间范围", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
