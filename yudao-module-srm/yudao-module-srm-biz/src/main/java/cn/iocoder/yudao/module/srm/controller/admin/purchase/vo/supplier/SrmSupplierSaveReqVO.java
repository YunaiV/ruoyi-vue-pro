package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 供应商新增/修改 Request VO")
@Data
public class SrmSupplierSaveReqVO {

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "供应商名称不能为空")
    private String name;

    @Schema(description = "联系人")
    private String contact;

    @Schema(description = "手机号码")
    @Length(max = 30, message = "手机号码长度不能超过 30 个字符")
    private String mobile;

    @Schema(description = "联系电话")
    @Length(max = 30, message = "联系电话长度不能超过 30 个字符")
    private String telephone;

    @Schema(description = "电子邮箱")
    @Email
    private String email;

    @Schema(description = "传真")
    private String fax;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开启状态不能为空")
    @InEnum(value = CommonStatusEnum.class)
    private Integer openStatus;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;

    @Schema(description = "纳税人识别号")
    private String taxNo;

    @Schema(description = "税率")
    private BigDecimal taxRate;

    @Schema(description = "开户行")
    private String bankName;

    @Schema(description = "开户账号")
    private String bankAccount;

    @Schema(description = "开户地址")
    private String bankAddress;

    @Schema(description = "付款条款ID")
    @NotNull(message = "付款条款ID不能为空")
    private Long paymentTermsId;

    @Schema(description = "送达地址")
    @NotBlank(message = "送达地址不能为空")
    private String deliveryAddress;

    @Schema(description = "公司地址")
    @NotBlank(message = "公司地址不能为空")
    private String companyAddress;
}