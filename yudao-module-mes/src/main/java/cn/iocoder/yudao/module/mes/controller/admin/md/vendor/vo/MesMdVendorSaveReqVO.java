package cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 供应商新增/修改 Request VO")
@Data
public class MesMdVendorSaveReqVO {

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "供应商编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "V00101")
    @NotEmpty(message = "供应商编码不能为空")
    private String code;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "海力德电子")
    @NotEmpty(message = "供应商名称不能为空")
    private String name;

    @Schema(description = "供应商简称", example = "海力德")
    private String nickname;

    @Schema(description = "供应商英文名称", example = "HLD Electronics")
    private String englishName;

    @Schema(description = "供应商简介", example = "专业从事电子元器件生产与销售")
    private String description;

    @Schema(description = "供应商LOGO地址", example = "https://xxx.com/logo.png")
    private String logo;

    @Schema(description = "供应商等级", example = "A")
    private String level;

    @Schema(description = "供应商评分", example = "95")
    private Integer score;

    @Schema(description = "供应商地址", example = "深圳市宝安区")
    private String address;

    @Schema(description = "供应商官网地址", example = "https://www.hld-elec.com")
    private String website;

    @Schema(description = "供应商邮箱地址", example = "info@hld-elec.com")
    private String email;

    @Schema(description = "供应商电话", example = "0755-12345678")
    private String telephone;

    @Schema(description = "联系人1", example = "王经理")
    private String contact1Name;

    @Schema(description = "联系人1-电话", example = "13800138001")
    private String contact1Telephone;

    @Schema(description = "联系人1-邮箱", example = "wang@hld-elec.com")
    private String contact1Email;

    @Schema(description = "联系人2", example = "赵助理")
    private String contact2Name;

    @Schema(description = "联系人2-电话", example = "13800138002")
    private String contact2Telephone;

    @Schema(description = "联系人2-邮箱", example = "zhao@hld-elec.com")
    private String contact2Email;

    @Schema(description = "统一社会信用代码", example = "91440300MA5EXAMPLE")
    private String creditCode;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
