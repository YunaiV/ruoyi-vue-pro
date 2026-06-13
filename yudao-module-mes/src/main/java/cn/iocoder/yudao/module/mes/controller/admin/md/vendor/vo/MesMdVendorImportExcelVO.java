package cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 供应商 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MesMdVendorImportExcelVO {

    @ExcelProperty("供应商编码")
    private String code;

    @ExcelProperty("供应商名称")
    private String name;

    @ExcelProperty("供应商简称")
    private String nickname;

    @ExcelProperty(value = "供应商等级", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_VENDOR_LEVEL)
    private String level;

    @ExcelProperty("供应商电话")
    private String telephone;

    @ExcelProperty("供应商邮箱地址")
    private String email;

    @ExcelProperty("供应商英文名称")
    private String englishName;

    @ExcelProperty("供应商简介")
    private String description;

    @ExcelProperty("供应商地址")
    private String address;

    @ExcelProperty("供应商官网地址")
    private String website;

    @ExcelProperty("联系人1")
    private String contact1Name;

    @ExcelProperty("联系人1-电话")
    private String contact1Telephone;

    @ExcelProperty("联系人1-邮箱")
    private String contact1Email;

    @ExcelProperty("联系人2")
    private String contact2Name;

    @ExcelProperty("联系人2-电话")
    private String contact2Telephone;

    @ExcelProperty("联系人2-邮箱")
    private String contact2Email;

    @ExcelProperty("统一社会信用代码")
    private String creditCode;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(cn.iocoder.yudao.module.system.enums.DictTypeConstants.COMMON_STATUS)
    private Integer status;

}
