package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP采购申请单新增/修改 Request VO")
@Data
public class PurchaseRequestSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "32561")
    private Long id;

    @Schema(description = "单据编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "单据编号不能为空")
    private String serial;

    @Schema(description = "当日申请排序编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "当日申请排序编号不能为空")
    private Integer num;

    @Schema(description = "申请人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "申请人不能为空")
    private String applicant;

    @Schema(description = "申请部门")
    private String applicationDept;

    @Schema(description = "单据日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单据日期不能为空")
    private LocalDateTime date;

    @Schema(description = "审核状态(0:待审核，1:审核通过，2:审核未通过)", example = "2")
    private Integer applicationStatus;

    @Schema(description = "关闭状态（0已关闭，1已开启）", example = "1")
    private Integer offStatus;

    @Schema(description = "订购状态（0部分订购，1全部订购）", example = "1")
    private Integer orderStatus;

    @Schema(description = "审核者")
    private String auditor;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

}