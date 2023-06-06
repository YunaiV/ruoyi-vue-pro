package cn.iocoder.yudao.module.jl.entity.user;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import lombok.*;
import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 用户信息 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "User")
@Table(name = "system_users")
public class User extends BaseEntity {

    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 用户账号
     */
    @Column(name = "username", nullable = false )
    private String username;

    /**
     * 密码
     */
    @Column(name = "password", nullable = false )
    private String password;

    /**
     * 用户昵称
     */
    @Column(name = "nickname", nullable = false )
    private String nickname;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;

    /**
     * 部门ID
     */
    @Column(name = "dept_id")
    private Long deptId;

    /**
     * 岗位编号数组
     */
    @Column(name = "post_ids")
    private String postIds;

    /**
     * 用户邮箱
     */
    @Column(name = "email")
    private String email;

    /**
     * 手机号码
     */
    @Column(name = "mobile")
    private String mobile;

    /**
     * 用户性别
     */
    @Column(name = "sex")
    private Byte sex;

    /**
     * 头像地址
     */
    @Column(name = "avatar")
    private String avatar;

    /**
     * 帐号状态（0正常 1停用）
     */
    @Column(name = "status", nullable = false )
    private Byte status;

    /**
     * 最后登录IP
     */
    @Column(name = "login_ip")
    private String loginIp;

    /**
     * 最后登录时间
     */
    @Column(name = "login_date")
    private LocalDateTime loginDate;

}
