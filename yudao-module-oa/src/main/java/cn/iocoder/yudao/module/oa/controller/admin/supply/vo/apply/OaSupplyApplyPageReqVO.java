package cn.iocoder.yudao.module.oa.controller.admin.supply.vo.apply;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.oa.enums.supply.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用品领用申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaSupplyApplyPageReqVO extends PageParam {

    @Schema(description = "申请单号", example = "YC202609140001")
    private String no;

    @Schema(description = "单据状态", example = "-1")
    private Integer status;

    @Schema(description = "申请部门编号", example = "1024")
    private Long deptId;

    @Schema(description = "创建时间", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
