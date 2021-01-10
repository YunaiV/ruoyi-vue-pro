package cn.iocoder.dashboard.modules.system.controller.dept.vo.post;

import cn.iocoder.dashboard.framework.excel.Excel;
import lombok.Data;

/**
 * 岗位 Excel 导出响应 VO
 */
@Data
public class SysPostExcelRespVO {

    @Excel(name = "岗位序号", cellType = Excel.ColumnType.NUMERIC)
    private Long id;

    @Excel(name = "岗位编码")
    private String code;

    @Excel(name = "岗位名称")
    private String name;

    @Excel(name = "岗位排序")
    private String sort;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

}
