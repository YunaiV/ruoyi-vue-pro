package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable;

import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.contract.CrmContractRespVO;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// TODO 芋艿：导出的 VO，可以考虑使用 @Excel 注解，实现导出功能
@Schema(description = "管理后台 - CRM 回款 Response VO")
@Data
public class CrmReceivableRespVO {

    @Schema(description = "编号", example = "25787")
    private Long id;

    @Schema(description = "回款编号", example = "31177")
    private String no;

    @Schema(description = "回款计划编号", example = "1024")
    private Long planId;

    @Schema(description = "回款方式", example = "2")
    private Integer returnType;

    @Schema(description = "回款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "9000")
    private BigDecimal price;

    @Schema(description = "计划回款日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-02-02")
    private LocalDateTime returnTime;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long customerId;
    @Schema(description = "客户名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "test")
    private String customerName;

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long contractId;
    @Schema(description = "合同信息")
    private CrmContractRespVO contract;

    @Schema(description = "负责人的用户编号", example = "25682")
    @ExcelProperty("负责人的用户编号")
    private Long ownerUserId;
    @Schema(description = "负责人名字", example = "25682")
    @ExcelProperty("负责人名字")
    private String ownerUserName;
    @Schema(description = "负责人部门")
    @ExcelProperty("负责人部门")
    private String ownerUserDeptName;

    @Schema(description = "工作流编号", example = "1043")
    @ExcelProperty("工作流编号")
    private String processInstanceId;

    @Schema(description = "审批状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer auditStatus;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    @Schema(description = "创建人", example = "25682")
    private String creator;
    @Schema(description = "创建人名字", example = "test")
    private String creatorName;

}
