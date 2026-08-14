package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 工资条发放记录 Response VO")
@Data
public class HrmSalarySlipSendRecordRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "工资表编号")
    private Long monthRecordId;

    @Schema(description = "计薪人数")
    private Integer employeeCount;

    @Schema(description = "发薪人数")
    private Integer sendEmployeeCount;

    @Schema(description = "已读人数")
    private Integer readCount;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "创建人编号")
    private String creator;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
