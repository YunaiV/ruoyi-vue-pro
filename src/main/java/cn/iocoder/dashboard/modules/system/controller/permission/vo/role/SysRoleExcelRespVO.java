package cn.iocoder.dashboard.modules.system.controller.permission.vo.role;

import cn.iocoder.dashboard.framework.excel.Excel;
import lombok.Data;

/**
 * 角色 Excel 导出响应 VO
 */
@Data
public class SysRoleExcelRespVO {

    @Excel(name = "角色序号", cellType = Excel.ColumnType.NUMERIC)
    private Long id;

    @Excel(name = "角色名称")
    private String name;

    @Excel(name = "角色标志")
    private String code;

    @Excel(name = "角色排序")
    private Integer sort;

    @Excel(name = "数据范围", readConverterExp = "1=所有数据权限,2=自定义数据权限,3=本部门数据权限,4=本部门及以下数据权限")
    private Integer dataScope;

    @Excel(name = "角色状态", readConverterExp = "0=正常,1=停用")
    private String status;

}
