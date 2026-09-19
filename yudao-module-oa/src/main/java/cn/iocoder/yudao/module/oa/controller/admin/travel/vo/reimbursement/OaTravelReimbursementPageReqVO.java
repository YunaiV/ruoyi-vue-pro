package cn.iocoder.yudao.module.oa.controller.admin.travel.vo.reimbursement;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 出差报销分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaTravelReimbursementPageReqVO extends PageParam {

    @Schema(description = "单据编号", example = "CLBX202609140001")
    private String no;

    @Schema(description = "审批状态", example = "-1")
    private Integer status;

    @Schema(description = "申请部门编号", example = "1024")
    private Long deptId;

    @Schema(description = "创建时间", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "支付状态", example = "false")
    private Boolean payStatus;


}
