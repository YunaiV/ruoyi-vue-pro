package cn.iocoder.yudao.module.mes.controller.admin.md.client.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - MES 客户新增/修改 Request VO")
@Data
public class MesMdClientSaveReqVO {

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "客户编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "C00184")
    @NotEmpty(message = "客户编码不能为空")
    @Size(max = 64, message = "客户编码长度不能超过 64 个字符")
    private String code;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "比亚迪")
    @NotEmpty(message = "客户名称不能为空")
    @Size(max = 255, message = "客户名称长度不能超过 255 个字符")
    private String name;

    @Schema(description = "客户简称", example = "比亚迪")
    @Size(max = 255, message = "客户简称长度不能超过 255 个字符")
    private String nickname;

    @Schema(description = "客户英文名称", example = "BYD")
    @Size(max = 255, message = "客户英文名称长度不能超过 255 个字符")
    private String englishName;

    @Schema(description = "客户简介", example = "比亚迪品牌诞生于深圳")
    @Size(max = 500, message = "客户简介长度不能超过 500 个字符")
    private String description;

    @Schema(description = "客户LOGO地址", example = "https://xxx.com/logo.png")
    @Size(max = 255, message = "客户LOGO地址长度不能超过 255 个字符")
    private String logo;

    @Schema(description = "客户类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "客户类型不能为空")
    private Integer type;

    @Schema(description = "客户地址", example = "深圳南山区")
    @Size(max = 500, message = "客户地址长度不能超过 500 个字符")
    private String address;

    @Schema(description = "客户官网地址", example = "https://www.bydglobal.com")
    @Size(max = 255, message = "客户官网地址长度不能超过 255 个字符")
    private String website;

    @Schema(description = "客户邮箱地址", example = "salse@bydglobal.com")
    @Size(max = 255, message = "客户邮箱地址长度不能超过 255 个字符")
    @Email(message = "客户邮箱地址格式不正确")
    private String email;

    @Schema(description = "客户电话", example = "123432222")
    @Size(max = 64, message = "客户电话长度不能超过 64 个字符")
    private String telephone;

    @Schema(description = "联系人1", example = "张三")
    @Size(max = 64, message = "联系人1长度不能超过 64 个字符")
    private String contact1Name;

    @Schema(description = "联系人1-电话", example = "122212312")
    @Size(max = 64, message = "联系人1-电话长度不能超过 64 个字符")
    private String contact1Telephone;

    @Schema(description = "联系人1-邮箱", example = "s1@bydglobal.com")
    @Size(max = 255, message = "联系人1-邮箱长度不能超过 255 个字符")
    @Email(message = "联系人1-邮箱格式不正确")
    private String contact1Email;

    @Schema(description = "联系人2", example = "李四")
    @Size(max = 64, message = "联系人2长度不能超过 64 个字符")
    private String contact2Name;

    @Schema(description = "联系人2-电话", example = "1132323232")
    @Size(max = 64, message = "联系人2-电话长度不能超过 64 个字符")
    private String contact2Telephone;

    @Schema(description = "联系人2-邮箱", example = "s2@bydglobal.com")
    @Size(max = 255, message = "联系人2-邮箱长度不能超过 255 个字符")
    @Email(message = "联系人2-邮箱格式不正确")
    private String contact2Email;

    @Schema(description = "统一社会信用代码", example = "11212121")
    @Size(max = 64, message = "统一社会信用代码长度不能超过 64 个字符")
    private String creditCode;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

}
