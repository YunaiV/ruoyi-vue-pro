package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user;

import java.util.Date;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import cn.iocoder.dashboard.framework.excel.Excel;
import cn.iocoder.dashboard.framework.excel.Excels;
import cn.iocoder.dashboard.framework.mybatis.core.BaseDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDept;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRole;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 用户 DO
 *
 * @author ruoyi
 */
@TableName("sys_user")
@Data
@EqualsAndHashCode(callSuper = true)
// TODO 芋艿：数据库的字段默认值
public class SysUserDO extends BaseDO {

    /**
     * 用户ID
     */
    @TableId
    @Excel(name = "用户序号", cellType = Excel.ColumnType.NUMERIC, prompt = "用户编号")
    private Long userId;

    /**
     * 部门ID
     */
    @Excel(name = "部门编号", type = Excel.Type.IMPORT)
    private Long deptId;

    /**
     * 用户账号
     */
    @Excel(name = "登录名称")
    @NotBlank(message = "用户账号不能为空")
    @Size(max = 30, message = "用户账号长度不能超过30个字符")
    private String username;

    /**
     * 加密后的密码
     *
     * 因为目前使用 {@link BCryptPasswordEncoder} 加密器，所以无需自己处理 salt 盐
     */
    private String password;

    /**
     * 用户昵称
     */
    @Excel(name = "用户名称")
    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    private String nickname;

    /**
     * 用户邮箱
     */
    @Excel(name = "用户邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    /**
     * 手机号码
     */
    @Excel(name = "手机号码")
    @Size(max = 11, message = "手机号码长度不能超过11个字符")
    private String mobile;

    /**
     * 用户性别
     */
    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 帐号状态（0正常 1停用）
     */
    @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用")
    // TODO 芋艿：修改成枚举
    private String status;

    /**
     * 最后登录IP
     */
    @Excel(name = "最后登录IP", type = Excel.Type.EXPORT)
    private String loginIp;

    /**
     * 最后登录时间
     */
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    private Date loginDate;


    // TODO FROM 芋艿：下面的字段，需要忽略

    /**
     * 部门对象
     */
    @Excels({
            @Excel(name = "部门名称", targetAttr = "deptName", type = Excel.Type.EXPORT),
            @Excel(name = "部门负责人", targetAttr = "leader", type = Excel.Type.EXPORT)
    })
    @TableField(exist = false)
    private SysDept dept;

    /**
     * 角色对象
     */
    @TableField(exist = false)
    private List<SysRole> roles;

    /**
     * 角色组
     */
    @TableField(exist = false)
    private Long[] roleIds;

    /**
     * 岗位组
     */
    @TableField(exist = false)
    private Long[] postIds;

    // TODO 芋艿：后续清理掉
    public boolean isAdmin() {
        return isAdmin(this.userId);
    }

    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

}
