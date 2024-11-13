package cn.iocoder.yudao.module.system.api.user.dto;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

/**
 * @className: AdminUserReqDTO
 * @author: Wqh
 * @date: 2024/11/4 14:45
 * @Version: 1.0
 * @description:
 */
@Data
public class AdminUserReqDTO {
    //id
    private Long id;

    //用户账号
    private String username;

    //用户昵称
    private String nickname;

    //备注
    private String remark;

    //部门编号
    private Long deptId;

    //岗位编号数组
    private Set<Long> postIds;

    //用户邮箱
    private String email;

    //手机号码
    private String mobile;

    //用户性别
    private Integer sex;

    //用户头像
    private String avatar;

    //密码
    private String password;
}
