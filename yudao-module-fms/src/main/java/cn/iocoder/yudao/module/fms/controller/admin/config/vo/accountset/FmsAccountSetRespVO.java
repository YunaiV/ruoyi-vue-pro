package cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - FMS 账套 Response VO")
@Data
public class FmsAccountSetRespVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "公司编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "WK001")
    private String companyCode;

    @Schema(description = "公司名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试科技有限公司")
    private String companyName;

    @Schema(description = "公司简介", example = "专注企业数字化服务")
    private String companyProfile;

    @Schema(description = "所在行业", example = "软件和信息技术服务业")
    private String industry;

    @Schema(description = "所在地", example = "上海市浦东新区")
    private String location;

    @Schema(description = "法人代表", example = "张三")
    private String legalRepresentative;

    @Schema(description = "法人身份证号", example = "310101199001011234")
    private String legalRepresentativeIdNumber;

    @Schema(description = "营业执照号", example = "91310000MA1K123456")
    private String businessLicenseNumber;

    @Schema(description = "组织机构代码", example = "MA1K12345")
    private String organizationCode;

    @Schema(description = "备注", example = "一般纳税人")
    private String remark;

    @Schema(description = "联系人", example = "李四")
    private String contactName;

    @Schema(description = "办公电话", example = "021-12345678")
    private String officeTelephone;

    @Schema(description = "手机号码", example = "15601691399")
    private String mobile;

    @Schema(description = "传真号码", example = "021-87654321")
    private String faxNumber;

    @Schema(description = "QQ 号码", example = "12345678")
    private String qqNumber;

    @Schema(description = "邮箱", example = "finance@example.com")
    private String email;

    @Schema(description = "其他联系方式", example = "企业微信：wk-finance")
    private String otherContact;

    @Schema(description = "详细地址", example = "张江高科技园区 1 号楼")
    private String address;

    @Schema(description = "币种编号", example = "1024")
    private Long currencyId;

    @Schema(description = "启用期间")
    private LocalDateTime startTime;

    @Schema(description = "会计制度", example = "1")
    private Integer standard;

    @Schema(description = "是否已初始化", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean initialized;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ==================== 来自 FmsAccountUserDO ====================

    @Schema(description = "是否默认账套", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean defaultStatus;

    @Schema(description = "是否账套创建人", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean founder;

    @Schema(description = "当前用户的成员权限级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer level;

}
