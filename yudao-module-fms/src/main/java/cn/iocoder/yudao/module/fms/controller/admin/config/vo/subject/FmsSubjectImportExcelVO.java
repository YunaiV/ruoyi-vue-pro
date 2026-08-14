package cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject;

import cn.idev.excel.annotation.ExcelProperty;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FMS 科目 Excel 导入 VO
 *
 * @author 芋道源码
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FmsSubjectImportExcelVO {

    @ExcelProperty("科目编码")
    @NotBlank(message = "科目编码不能为空")
    private String code;

    @ExcelProperty("科目名称")
    @NotBlank(message = "科目名称不能为空")
    private String name;

    @ExcelProperty("上级科目编码")
    private String parentSubjectCode;

    @ExcelProperty("余额方向")
    @NotBlank(message = "余额方向不能为空")
    private String balanceDirection;

    @ExcelProperty("类别")
    @NotBlank(message = "类别不能为空")
    private String categoryName;

    @ExcelProperty("辅助核算")
    private String auxiliaryNames;

}
