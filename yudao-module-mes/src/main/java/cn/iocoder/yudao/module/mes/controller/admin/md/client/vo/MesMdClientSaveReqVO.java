package cn.iocoder.yudao.module.mes.controller.admin.md.client.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 客户新增/修改 Request VO")
@Data
public class MesMdClientSaveReqVO {

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "客户编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "C00184")
    @NotEmpty(message = "客户编码不能为空")
    private String code;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "比亚迪")
    @NotEmpty(message = "客户名称不能为空")
    private String name;

    @Schema(description = "客户简称", example = "比亚迪")
    private String nickname;

    @Schema(description = "客户英文名称", example = "BYD")
    private String englishName;

    @Schema(description = "客户简介", example = "比亚迪品牌诞生于深圳")
    private String description;

    @Schema(description = "客户LOGO地址", example = "https://xxx.com/logo.png")
    private String logo;

    @Schema(description = "客户类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "客户类型不能为空")
    private Integer type;

    @Schema(description = "客户地址", example = "深圳南山区")
    private String address;

    @Schema(description = "客户官网地址", example = "https://www.bydglobal.com")
    private String website;

    @Schema(description = "客户邮箱地址", example = "salse@bydglobal.com")
    private String email;

    @Schema(description = "客户电话", example = "123432222")
    private String telephone;

    @Schema(description = "联系人1", example = "张三")
    private String contact1Name;

    @Schema(description = "联系人1-电话", example = "122212312")
    private String contact1Telephone;

    @Schema(description = "联系人1-邮箱", example = "s1@bydglobal.com")
    private String contact1Email;

    @Schema(description = "联系人2", example = "李四")
    private String contact2Name;

    @Schema(description = "联系人2-电话", example = "1132323232")
    private String contact2Telephone;

    @Schema(description = "联系人2-邮箱", example = "s2@bydglobal.com")
    private String contact2Email;

    @Schema(description = "统一社会信用代码", example = "11212121")
    private String creditCode;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
