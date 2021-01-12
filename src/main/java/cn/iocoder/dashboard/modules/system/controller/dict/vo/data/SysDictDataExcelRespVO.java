package cn.iocoder.dashboard.modules.system.controller.dict.vo.data;

import cn.iocoder.dashboard.framework.excel.Excel;
import lombok.Data;

/**
 * 字典数据 Excel 导出响应 VO
 */
@Data
public class SysDictDataExcelRespVO {

    @Excel(name = "字典编码", cellType = Excel.ColumnType.NUMERIC)
    private Long id;

    @Excel(name = "字典排序", cellType = Excel.ColumnType.NUMERIC)
    private Integer sort;

    @Excel(name = "字典标签")
    private String label;

    @Excel(name = "字典键值")
    private String value;

    @Excel(name = "字典类型")
    private String dictType;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private Integer status;

}
