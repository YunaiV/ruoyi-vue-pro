package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工联系人 Response VO")
@Data
public class HrmEmployeeContactRespVO {

    @Schema(description = "联系人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long employeeId;

    @Schema(description = "联系人名称", example = "李四")
    private String name;

    @Schema(description = "关系", example = "配偶")
    private String relation;

    @Schema(description = "联系人电话", example = "15601691301")
    private String phone;

    @Schema(description = "联系人工作单位")
    private String workUnit;

    @Schema(description = "联系人职务")
    private String postName;

    @Schema(description = "联系人地址")
    private String address;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
