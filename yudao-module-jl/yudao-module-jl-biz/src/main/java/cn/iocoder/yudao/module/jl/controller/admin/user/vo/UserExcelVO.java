package cn.iocoder.yudao.module.jl.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 用户信息 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class UserExcelVO {

    @ExcelProperty("用户ID")
    private Long id;

    @ExcelProperty("用户账号")
    private String username;

    @ExcelProperty("用户昵称")
    private String nickname;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("部门ID")
    private Long deptId;

    @ExcelProperty("岗位编号数组")
    private String postIds;

    @ExcelProperty("用户邮箱")
    private String email;

    @ExcelProperty("手机号码")
    private String mobile;

    @ExcelProperty("用户性别")
    private Byte sex;

    @ExcelProperty("头像地址")
    private String avatar;

    @ExcelProperty("帐号状态（0正常 1停用）")
    private Byte status;

}
