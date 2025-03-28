package cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Schema(description = "管理后台 - Erp财务主体新增/修改 Request VO")
@Data
public class FmsFinanceSubjectSaveReqVO {
    @Schema(description = "id")
    @Null(groups = Validation.OnCreate.class, message = "创建时，id必须为空")
    @NotNull(groups = Validation.OnUpdate.class, message = "更新时，id不能为空")
    private Long id;

    @Schema(description = "主体名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "主体名称不能为空")
    private String name;

    @Schema(description = "联系人")
    private String contact;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "联系电话")
    private String telephone;

    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "传真")
    private String fax;

    @Schema(description = "送达地址")
    private String deliveryAddress;

    @Schema(description = "公司地址")
    private String companyAddress;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开启状态不能为空")
    private Boolean status;

    @Schema(description = "纳税人识别号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "纳税人识别号不能为空")
    private String taxNo;

    @Schema(description = "开户行")
    private String bankName;

    @Schema(description = "开户账号")
    private String bankAccount;

    @Schema(description = "开户地址")
    private String bankAddress;

}