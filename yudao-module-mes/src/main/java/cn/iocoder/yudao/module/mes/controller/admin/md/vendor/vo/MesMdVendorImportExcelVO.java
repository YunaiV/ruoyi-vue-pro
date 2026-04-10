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

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(cn.iocoder.yudao.module.system.enums.DictTypeConstants.COMMON_STATUS)
    private Integer status;

}
