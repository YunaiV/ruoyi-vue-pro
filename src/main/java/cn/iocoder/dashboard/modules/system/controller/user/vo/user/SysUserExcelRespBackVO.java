package cn.iocoder.dashboard.modules.system.controller.user.vo.user;

import cn.iocoder.dashboard.framework.excel.Excel;
import cn.iocoder.dashboard.framework.excel.Excels;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDeptDO;
import lombok.Data;

import java.util.Date;

/**
 * 用户 Excel 导出响应 VO
 */
@Data
public class SysUserExcelRespBackVO {

    @Excel(name = "用户序号", cellType = Excel.ColumnType.NUMERIC, prompt = "用户编号")
    private Long id;

    @Excel(name = "登录名称")
    private String username;

    @Excel(name = "用户名称")
    private String nickname;

    @Excel(name = "部门编号", type = Excel.Type.IMPORT)
    private Long deptId;

    @Excel(name = "用户邮箱")
    private String email;

    @Excel(name = "手机号码")
    private String mobile;

    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用")
    private Integer status;

    @Excel(name = "最后登录IP", type = Excel.Type.EXPORT)
    private String loginIp;

    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    private Date loginDate;

    @Excels({
            @Excel(name = "部门名称", targetAttr = "deptName", type = Excel.Type.EXPORT),
            @Excel(name = "部门负责人", targetAttr = "leader", type = Excel.Type.EXPORT)
    })
    private SysDeptDO dept;

}
