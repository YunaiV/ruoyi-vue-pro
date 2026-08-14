package cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem;

import cn.idev.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * FMS 辅助核算项目 Excel 导入 VO
 *
 * @author 芋道源码
 */
@Data
public class FmsAuxiliaryItemImportExcelVO {

    @ExcelProperty("编码")
    @NotBlank(message = "编码不能为空")
    @Size(max = 64, message = "编码长度不能超过 64 个字符")
    private String code;

    @ExcelProperty("名称")
    @NotBlank(message = "名称不能为空")
    @Size(max = 255, message = "名称长度不能超过 255 个字符")
    private String name;

    @ExcelProperty("备注")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    @ExcelProperty("规格")
    @Size(max = 255, message = "规格长度不能超过 255 个字符")
    private String specification;

    @ExcelProperty("单位")
    @Size(max = 255, message = "单位长度不能超过 255 个字符")
    private String unit;

}
