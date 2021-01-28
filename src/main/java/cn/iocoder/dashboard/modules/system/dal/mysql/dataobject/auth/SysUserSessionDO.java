package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.auth;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user.SysUserDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 在线用户表
 */
@TableName(value = "sys_user_session", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserSessionDO extends BaseDO {

    /**
     * 会话编号, 即 sessionId
     */
    @TableId
    private String id;
    /**
     * 用户编号
     *
     * 外键 {@link SysUserDO#getId()}
     */
    private Long userId;
    /**
     * 用户 IP
     */
    private String userIp;
    /**
     * 浏览器 UA
     */
    private String userAgent;

}
