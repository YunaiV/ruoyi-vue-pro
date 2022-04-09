package cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 敏感词 Excel VO
 *
 * @author 永不言败
 */
@Data
public class SensitiveWordExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("敏感词")
    private String name;

    @ExcelProperty("标签")
    private List<String> tags;

    @ExcelProperty("状态，true-启用，false-禁用")
    private Integer status;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("创建时间")
    private Date createTime;

}
