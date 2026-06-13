package cn.iocoder.yudao.module.mes.controller.admin.md.client.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MesMdClientImportExcelVO {

    @ExcelProperty("客户编码")
    private String code;

    @ExcelProperty("客户名称")
    private String name;

    @ExcelProperty("客户简称")
    private String nickname;

    @ExcelProperty(value = "客户类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_CLIENT_TYPE)
    private Integer type;

    @ExcelProperty("客户电话")
    private String telephone;

    @ExcelProperty("客户邮箱地址")
    private String email;

    @ExcelProperty("客户英文名称")
    private String englishName;

    @ExcelProperty("客户简介")
    private String description;

    @ExcelProperty("客户地址")
    private String address;

    @ExcelProperty("客户官网地址")
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
