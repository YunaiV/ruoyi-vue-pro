package cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 供应商 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesMdVendorRespVO {

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("供应商编号")
    private Long id;

    @Schema(description = "供应商编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "V00101")
    @ExcelProperty("供应商编码")
    private String code;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "海力德电子")
    @ExcelProperty("供应商名称")
    private String name;

    @Schema(description = "供应商简称", example = "海力德")
    @ExcelProperty("供应商简称")
    private String nickname;

    @Schema(description = "供应商英文名称", example = "HLD Electronics")
    @ExcelProperty("供应商英文名称")
    private String englishName;

    @Schema(description = "供应商简介", example = "专业从事电子元器件生产与销售")
    @ExcelProperty("供应商简介")
    private String description;

    @Schema(description = "供应商LOGO地址", example = "https://xxx.com/logo.png")
    private String logo;

    @Schema(description = "供应商等级", example = "A")
    @ExcelProperty(value = "供应商等级", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_VENDOR_LEVEL)
    private String level;

    @Schema(description = "供应商评分", example = "95")
    @ExcelProperty("供应商评分")
    private Integer score;

    @Schema(description = "供应商地址", example = "深圳市宝安区")
    @ExcelProperty("供应商地址")
    private String address;

    @Schema(description = "供应商官网地址", example = "https://www.hld-elec.com")
    @ExcelProperty("供应商官网地址")
    private String website;

    @Schema(description = "供应商邮箱地址", example = "info@hld-elec.com")
    @ExcelProperty("供应商邮箱地址")
    private String email;

    @Schema(description = "供应商电话", example = "0755-12345678")
    @ExcelProperty("供应商电话")
    private String telephone;

    @Schema(description = "联系人1", example = "王经理")
    @ExcelProperty("联系人1")
    private String contact1Name;

    @Schema(description = "联系人1-电话", example = "13800138001")
    @ExcelProperty("联系人1-电话")
    private String contact1Telephone;

    @Schema(description = "联系人1-邮箱", example = "wang@hld-elec.com")
    @ExcelProperty("联系人1-邮箱")
    private String contact1Email;

    @Schema(description = "联系人2", example = "赵助理")
    @ExcelProperty("联系人2")
    private String contact2Name;

    @Schema(description = "联系人2-电话", example = "13800138002")
    @ExcelProperty("联系人2-电话")
    private String contact2Telephone;

    @Schema(description = "联系人2-邮箱", example = "zhao@hld-elec.com")
    @ExcelProperty("联系人2-邮箱")
    private String contact2Email;

    @Schema(description = "统一社会信用代码", example = "91440300MA5EXAMPLE")
    @ExcelProperty("统一社会信用代码")
    private String creditCode;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(cn.iocoder.yudao.module.system.enums.DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
