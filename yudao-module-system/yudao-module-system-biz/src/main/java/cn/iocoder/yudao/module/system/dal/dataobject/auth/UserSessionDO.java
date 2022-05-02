package cn.iocoder.yudao.module.system.dal.dataobject.auth;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 在线用户表
 *
 * 我们已经将 {@link LoginUser} 缓存在 Redis 当中。
 * 这里额外存储在线用户到 MySQL 中，目的是为了方便管理界面可以灵活查询。
 * 同时，通过定时轮询 UserSessionDO 表，可以主动删除 Redis 的缓存，因为 Redis 的过期删除是延迟的。
 *
 * @author 芋道源码
 */
@TableName(value = "system_user_session")
@KeySequence(value = "system_user_session_seq")
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserSessionDO extends BaseDO {

    /**
     * 会话编号
     */
    private Long id;
    /**
     * 令牌
     */
    private String token;

    /**
     * 用户编号
     *
     * 关联 AdminUserDO.id 或者 MemberUserDO.id
     */
    private Long userId;
    /**
     * 用户类型
     *
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;

    /**
     * 用户账号
     *
     * 冗余，因为账号可以变更
     */
    private String username;

    /**
     * 用户 IP
     */
    private String userIp;
    /**
     * 浏览器 UA
     */
    private String userAgent;
    /**
     * 会话超时时间
     */
    private Date sessionTimeout;

}
