package cn.iocoder.yudao.module.oa.controller.admin.plan.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.plan.OaPlanTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - OA 工作计划报表分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaPlanReportPageReqVO extends PageParam {

    @Schema(description = "成员昵称", example = "芋道")
    private String userName;

    @Schema(description = "计划类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计划类型不能为空")
    @InEnum(value = OaPlanTypeEnum.class, message = "计划类型必须是 {value}")
    private Integer type;

    @Schema(description = "创建时间范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @NotNull(message = "创建时间范围不能为空")
    @Size(min = 2, max = 2, message = "创建时间范围格式不正确")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
