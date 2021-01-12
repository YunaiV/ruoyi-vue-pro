package cn.iocoder.dashboard.modules.system.controller.dict.vo.type;

import cn.iocoder.dashboard.framework.excel.Excel;
import lombok.Data;

/**
 * 字典类型 Excel 导出响应 VO
 */
@Data
public class SysDictTypeExcelRespVO {

    @Excel(name = "字典主键", cellType = Excel.ColumnType.NUMERIC)
    private Long id;

    @Excel(name = "字典名称")
    private String name;

    @Excel(name = "字典类型")
    private String type;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private Integer status;

}
