package cn.iocoder.yudao.module.oa.controller.admin.seal.vo.apply;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 用印申请分页 Request VO")
public class OaSealApplyPageReqVO extends PageParam {

    @Schema(description = "申请单号", example = "SEAL-APPLY-001")
    private String no;

    @Schema(description = "印章编号", example = "1024")
    private Long sealId;

    @Schema(description = "印章名称", example = "公司公章")
    private String sealName;

    @Schema(description = "申请部门编号", example = "100")
    private Long deptId;

    @Schema(description = "创建时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "预计用印时间范围", example = "[\"2026-09-11 09:00:00\", \"2026-09-11 18:00:00\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] expectedUseTime;

    @Schema(description = "审批状态", example = "-1")
    private Integer status;

    @Schema(description = "用印状态", example = "0")
    private Integer useStatus;

    @Schema(description = "用印类型", example = "1")
    private Integer type;

    @Schema(description = "用印方式", example = "1")
    private Integer mode;

    @Schema(description = "紧急", example = "false")
    private Boolean urgent;
}
