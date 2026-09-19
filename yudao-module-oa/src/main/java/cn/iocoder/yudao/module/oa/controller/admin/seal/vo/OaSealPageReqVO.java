package cn.iocoder.yudao.module.oa.controller.admin.seal.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 印章分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaSealPageReqVO extends PageParam {

    @Schema(description = "编号", example = "SEAL-001")
    private String no;

    @Schema(description = "名称", example = "公司公章")
    private String name;

    @Schema(description = "分类", example = "1")
    private Integer category;

    @Schema(description = "类型", example = "1")
    private Integer type;

    @Schema(description = "保管人编号", example = "1024")
    private Long keeperUserId;

    @Schema(description = "所属部门编号", example = "1024")
    private Long deptId;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "购买时间范围", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] purchaseTime;

    @Schema(description = "启用时间范围", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] enableTime;

    @Schema(description = "停用时间范围", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] disableTime;

}
