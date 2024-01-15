package cn.iocoder.yudao.module.crm.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

// TODO @puhui999：导出注解哈
@Schema(description = "管理后台 - CRM 合同 Response VO")
@Data
public class CrmContractRespVO {

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    private Long id;

    @Schema(description = "合同名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    private String name;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18336")
    private Long customerId;

    @Schema(description = "商机编号", example = "10864")
    private Long businessId;

    @Schema(description = "工作流编号", example = "1043")
    private Long processInstanceId;

    @Schema(description = "下单日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime orderDate;

    @Schema(description = "负责人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17144")
    private Long ownerUserId;

    // TODO @芋艿：未来应该支持自动生成；
    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20230101")
    private String no;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "合同金额", example = "5617")
    private Integer price;

    @Schema(description = "整单折扣")
    private Integer discountPercent;

    @Schema(description = "产品总金额", example = "19510")
    private Integer productPrice;

    @Schema(description = "联系人编号", example = "18546")
    private Long contactId;

    @Schema(description = "公司签约人", example = "14036")
    private Long signUserId;

    @Schema(description = "最后跟进时间")
    private LocalDateTime contactLastTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "创建人", example = "25682")
    private String creator;

    @Schema(description = "创建人名字", example = "test")
    private String creatorName;

    @Schema(description = "客户名字", example = "test")
    private String customerName;

    @Schema(description = "负责人", example = "test")
    private String ownerUserName;

    @Schema(description = "审批状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer auditStatus;

}
