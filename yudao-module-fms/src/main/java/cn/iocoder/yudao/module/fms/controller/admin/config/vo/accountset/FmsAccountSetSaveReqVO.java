package cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - FMS 账套新增/修改 Request VO")
@Data
public class FmsAccountSetSaveReqVO {

    @Schema(description = "账套编号", example = "1024")
    private Long id;

    @Schema(description = "公司编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "WK001")
    @NotBlank(message = "公司编码不能为空")
    @Size(max = 64, message = "公司编码不能超过 64 个字符")
    @DiffLogField(name = "公司编码")
    private String companyCode;

    @Schema(description = "公司名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试科技有限公司")
    @NotBlank(message = "公司名称不能为空")
    @Size(max = 255, message = "公司名称不能超过 255 个字符")
    @DiffLogField(name = "公司名称")
    private String companyName;

    @Schema(description = "公司简介", example = "专注企业数字化服务")
    @Size(max = 500, message = "公司简介不能超过 500 个字符")
    @DiffLogField(name = "公司简介")
    private String companyProfile;

    @Schema(description = "所在行业", example = "软件和信息技术服务业")
    @Size(max = 255, message = "所在行业不能超过 255 个字符")
    @DiffLogField(name = "所在行业")
    private String industry;

    @Schema(description = "所在地", example = "上海市浦东新区")
    @Size(max = 255, message = "所在地不能超过 255 个字符")
    @DiffLogField(name = "所在地")
    private String location;

    @Schema(description = "法人代表", example = "张三")
    @Size(max = 255, message = "法人代表不能超过 255 个字符")
    @DiffLogField(name = "法人代表")
    private String legalRepresentative;

    @Schema(description = "法人身份证号", example = "310101199001011234")
    @Size(max = 255, message = "法人身份证号不能超过 255 个字符")
    @DiffLogField(name = "法人身份证号")
    private String legalRepresentativeIdNumber;

    @Schema(description = "营业执照号", example = "91310000MA1K123456")
    @Size(max = 255, message = "营业执照号不能超过 255 个字符")
    @DiffLogField(name = "营业执照号")
    private String businessLicenseNumber;

    @Schema(description = "组织机构代码", example = "MA1K12345")
    @Size(max = 255, message = "组织机构代码不能超过 255 个字符")
    @DiffLogField(name = "组织机构代码")
    private String organizationCode;

    @Schema(description = "备注", example = "一般纳税人")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    @DiffLogField(name = "备注")
    private String remark;

    @Schema(description = "联系人", example = "李四")
    @Size(max = 255, message = "联系人不能超过 255 个字符")
    @DiffLogField(name = "联系人")
    private String contactName;

    @Schema(description = "办公电话", example = "021-12345678")
    @Size(max = 32, message = "办公电话不能超过 32 个字符")
    @DiffLogField(name = "办公电话")
    private String officeTelephone;

    @Schema(description = "手机号码", example = "15601691399")
    @Size(max = 32, message = "手机号码不能超过 32 个字符")
    @DiffLogField(name = "手机号码")
    private String mobile;

    @Schema(description = "传真号码", example = "021-87654321")
    @Size(max = 32, message = "传真号码不能超过 32 个字符")
    @DiffLogField(name = "传真号码")
    private String faxNumber;

    @Schema(description = "QQ 号码", example = "12345678")
    @Size(max = 255, message = "QQ 号码不能超过 255 个字符")
    @DiffLogField(name = "QQ 号码")
    private String qqNumber;

    @Schema(description = "邮箱", example = "finance@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱不能超过 255 个字符")
    @DiffLogField(name = "邮箱")
    private String email;

    @Schema(description = "其他联系方式", example = "企业微信：wk-finance")
    @Size(max = 255, message = "其他联系方式不能超过 255 个字符")
    @DiffLogField(name = "其他联系方式")
    private String otherContact;

    @Schema(description = "详细地址", example = "张江高科技园区 1 号楼")
    @Size(max = 255, message = "详细地址不能超过 255 个字符")
    @DiffLogField(name = "详细地址")
    private String address;

}
