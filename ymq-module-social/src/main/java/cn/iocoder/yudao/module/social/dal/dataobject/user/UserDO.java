package cn.iocoder.yudao.module.social.dal.dataobject.user;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * C 端用户 DO（与运营后台 system_users 完全分离）。
 *
 * 字段定义见 docs/5-数据库设计.md §2.1。
 * 不继承 BaseDO/TenantBaseDO：YMQ 是 C 端单租户应用，且字段命名沿用项目数据库设计文档（created_at/updated_at 由 MySQL 默认值维护）。
 */
@TableName("t_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDO implements Serializable {

    @TableId
    private Long id;

    private String openid;
    private String unionid;
    private String nickname;
    private String avatarUrl;

    /** 0未知 1男 2女 */
    private Integer gender;
    /** 手机号（实名时获取） */
    private String phone;
    /** 是否实名（绑定 phone） */
    private Integer isRealNameVerified;

    /** 当前段位 D1/D2/.../S */
    private String currentLevel;
    private Integer currentScore;
    private Integer totalGames;
    private Integer wins;
    /** 已组织活动数（召集人成长等级） */
    private Integer totalOrganized;

    /** 信用分（默认 100，范围 0-150） */
    private Integer creditScore;

    private BigDecimal lastKnownLng;
    private BigDecimal lastKnownLat;
    /** 行政区划代码（如 320582） */
    private String regionCode;
    private String regionName;
    private LocalDateTime lastLocationAt;

    /** 女性可见设置 0仅活动内 1完全公开 */
    private Integer femaleVisibility;
    /** 是否参与附近发现 */
    private Integer discoverEnabled;

    /** 1正常 0封禁 */
    private Integer status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
