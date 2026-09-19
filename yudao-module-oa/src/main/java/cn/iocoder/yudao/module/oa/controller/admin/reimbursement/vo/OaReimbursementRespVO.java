package cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo;

import cn.iocoder.yudao.module.oa.dal.dataobject.reimbursement.OaReimbursementDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 费用报销 Response VO")
@Data
public class OaReimbursementRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "标题", example = "客户项目交通费报销")
    private String title;

    @Schema(description = "紧急程度", example = "1")
    private Integer urgency;

    @Schema(description = "申请原因", example = "报销客户项目验收期间的交通费用")
    private String reason;

    @Schema(description = "证明人用户编号", example = "1024")
    private Long witnessUserId;

    @Schema(description = "相关客户", example = "芋道科技有限公司")
    private String customerName;

    @Schema(description = "报销方式", example = "1")
    private Integer paymentMethod;

    @Schema(description = "票据总数", example = "1")
    private Integer invoiceCount;

    @Schema(description = "报销总金额", example = "100.00")
    private BigDecimal totalPrice;

    @Schema(description = "审批状态", example = "-1")
    private Integer status;

    @Schema(description = "流程实例编号", example = "1024")
    private String processInstanceId;

    @Schema(description = "报销明细")
    private List<OaReimbursementDO.Item> items;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    private List<String> fileUrls;

    @Schema(description = "申请人编号", example = "1024")
    private String creator;

    @Schema(description = "申请人昵称", example = "张三")
    private String creatorName;

    @Schema(description = "申请时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;
}
