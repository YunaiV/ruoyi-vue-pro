package cn.iocoder.yudao.module.oa.controller.admin.seal.vo.apply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "管理后台 - 用印申请 Response VO")
public class OaSealApplyRespVO {

    @Schema(description = "编号", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "印章编号", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sealId;

    @Schema(description = "用印事由", example = "合同签署用印", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "用印类型", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "用印方式", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer mode;

    @Schema(description = "文件标题", example = "采购合同")
    private String documentTitle;

    @Schema(description = "文件类型", example = "合同")
    private String documentType;

    @Schema(description = "文件份数", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer documentCount;

    @Schema(description = "合同金额", example = "10000.00")
    private BigDecimal contractPrice;

    @Schema(description = "合同对方", example = "示例公司")
    private String contractParty;

    @Schema(description = "预计用印时间", example = "1789088400000", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64")
    private LocalDateTime expectedUseTime;

    @Schema(description = "预计归还时间", example = "1789120800000", type = "integer", format = "int64")
    private LocalDateTime expectedReturnTime;

    @Schema(description = "是否紧急", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean urgent;

    @Schema(description = "备注", example = "合同签署完成后归档")
    private String remark;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/contract.pdf\"]")
    private List<String> fileUrls;

    @Schema(description = "申请单号", example = "SEAL-APPLY-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String no;

    @Schema(description = "印章编号", example = "SEAL-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sealNo;

    @Schema(description = "印章名称", example = "公司公章", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sealName;

    @Schema(description = "印章类型", example = "1")
    private Integer sealType;

    @Schema(description = "保管部门编号", example = "100")
    private Long keeperDeptId;

    @Schema(description = "保管部门", example = "办公室")
    private String keeperDeptName;

    @Schema(description = "申请人编号", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "申请部门编号", example = "1024")
    private Long deptId;

    @Schema(description = "申请人", example = "芋道")
    private String userName;

    @Schema(description = "申请部门", example = "研发部门")
    private String deptName;

    @Schema(description = "保管人编号", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long keeperUserId;

    @Schema(description = "保管人", example = "芋道")
    private String keeperName;

    @Schema(description = "审批状态", example = "-1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "用印状态", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer useStatus;

    @Schema(description = "流程实例编号", example = "b3bb1b18-6001-11ef-9d05-0242ac120002")
    private String processInstanceId;

    @Schema(description = "实际用印时间", example = "1789088400000", type = "integer", format = "int64")
    private LocalDateTime actualUseTime;

    @Schema(description = "实际归还时间", example = "1789120800000", type = "integer", format = "int64")
    private LocalDateTime actualReturnTime;

    @Schema(description = "创建时间", example = "1789088400000", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64")
    private LocalDateTime createTime;
}
