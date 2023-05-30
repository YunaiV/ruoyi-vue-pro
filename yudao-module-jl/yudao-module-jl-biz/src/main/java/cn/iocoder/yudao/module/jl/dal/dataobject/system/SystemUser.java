package cn.iocoder.yudao.module.jl.dal.dataobject.system;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@Entity(name = "SystemUser")
@Table(name = "system_users")
public class SystemUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 30)
    @NotNull
    @Column(name = "username", nullable = false, length = 30)
    private String username;

    @Size(max = 30)
    @NotNull
    @Column(name = "nickname", nullable = false, length = 30)
    private String nickname;

    @Size(max = 500)
    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "dept_id")
    private Long deptId;

    @Size(max = 255)
    @Column(name = "post_ids")
    private String postIds;

    @Size(max = 50)
    @Column(name = "email", length = 50)
    private String email;

    @Size(max = 11)
    @Column(name = "mobile", length = 11)
    private String mobile;

    @Column(name = "sex")
    private Byte sex;

    @Size(max = 512)
    @Column(name = "avatar", length = 512)
    private String avatar;

    @NotNull
    @Column(name = "status", nullable = false)
    private Byte status;

    @NotNull
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

}