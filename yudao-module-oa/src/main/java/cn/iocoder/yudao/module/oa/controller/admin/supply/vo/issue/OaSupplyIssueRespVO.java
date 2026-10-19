package cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用品领用发放 Response VO")
@Data
public class OaSupplyIssueRespVO {

    @Schema(description = "明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long applyId;

    @Schema(description = "用品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long itemId;

    @Schema(description = "物品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "A4 打印纸")
    private String itemName;

    @Schema(description = "规格型号", example = "A4 80g")
    private String model;

    @Schema(description = "计量单位", example = "包")
    private String unit;

    @Schema(description = "管理类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer manageType;

    @Schema(description = "申请数量", example = "1")
    private Integer applyQuantity;

    @Schema(description = "实发数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer issuedQuantity;

    @Schema(description = "已归还数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer returnedQuantity;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "-1")
    private Integer status;

    @Schema(description = "发放人编号", example = "1024")
    private Long issueUserId;

    @Schema(description = "发放人", example = "张三")
    private String issueUserName;

    @Schema(description = "发放时间", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime issueTime;

    @Schema(description = "发放备注", example = "已按申请明细发放")
    private String issueRemark;

    @Schema(description = "归还备注", example = "物品完好，已归还入库")
    private String returnRemark;

    @Schema(description = "申请单号", example = "YC202609140001")
    private String no;

    @Schema(description = "申请人", example = "张三")
    private String creatorName;

    @Schema(description = "申请部门", example = "行政部")
    private String deptName;

    @Schema(description = "使用类型", example = "1")
    private Integer useType;

    @Schema(description = "申请时间", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}
