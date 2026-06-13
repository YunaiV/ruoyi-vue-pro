package cn.iocoder.yudao.module.wms.controller.admin.md.merchant.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.md.WmsMerchantTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - WMS 往来企业创建/更新 Request VO")
@Data
public class WmsMerchantSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "往来企业编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "MER001")
    @NotBlank(message = "往来企业编号不能为空")
    @Size(max = 20, message = "往来企业编号长度不能超过 20 个字符")
    private String code;

    @Schema(description = "往来企业名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "华为")
    @NotBlank(message = "往来企业名称不能为空")
    @Size(max = 60, message = "往来企业名称长度不能超过 60 个字符")
    private String name;

    @Schema(description = "往来企业类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "往来企业类型不能为空")
    @InEnum(value = WmsMerchantTypeEnum.class, message = "往来企业类型必须是 {value}")
    private Integer type;

    @Schema(description = "级别", example = "A")
    @Size(max = 10, message = "级别长度不能超过 10 个字符")
    private String level;

    @Schema(description = "开户行", example = "招商银行")
    @Size(max = 255, message = "开户行长度不能超过 255 个字符")
    private String bankName;

    @Schema(description = "银行账户", example = "6225888888888888")
    @Size(max = 40, message = "银行账户长度不能超过 40 个字符")
    private String bankAccount;

    @Schema(description = "地址", example = "深圳市南山区")
    @Size(max = 200, message = "地址长度不能超过 200 个字符")
    private String address;

    @Schema(description = "手机号", example = "15601691300")
    @Size(max = 13, message = "手机号长度不能超过 13 个字符")
    private String mobile;

    @Schema(description = "座机号", example = "0755-88888888")
    @Size(max = 13, message = "座机号长度不能超过 13 个字符")
    private String telephone;

    @Schema(description = "联系人", example = "王五")
    @Size(max = 30, message = "联系人长度不能超过 30 个字符")
    private String contact;

    @Schema(description = "Email", example = "wms@example.com")
    @Size(max = 50, message = "Email 长度不能超过 50 个字符")
    private String email;

    @Schema(description = "备注", example = "备注")
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    private String remark;

}
