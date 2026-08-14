package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.contract;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 员工合同 Response VO")
@Data
public class HrmEmployeeContractRespVO {

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long employeeId;

    @Schema(description = "合同编号", example = "HT-2026-001")
    private String no;

    @Schema(description = "合同类型", example = "1")
    private Integer type;

    @Schema(description = "合同开始日期")
    private LocalDateTime startTime;

    @Schema(description = "合同结束日期")
    private LocalDateTime endTime;

    @Schema(description = "期限", example = "36")
    private Integer term;

    @Schema(description = "合同状态", example = "1")
    private Integer status;

    @Schema(description = "签约公司")
    private String signCompany;

    @Schema(description = "合同签订日期")
    private LocalDateTime signTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否到期提醒", example = "true")
    private Boolean expireRemind;

    @Schema(description = "附件地址数组")
    private List<String> fileUrls;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
