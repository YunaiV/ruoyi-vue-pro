package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.enums.logger.SysLoginResultEnum;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 系统访问记录表
 *
 * @author ruoyi
 */
@TableName("用户登陆日志")
public class SysLoginLogDO extends BaseDO {

    /**
     * 日志主键
     */
    private Long id;
    /**
     * 链路追踪编号
     */
    private String traceId;
    /**
     * 用户编号
     *
     * 外键 {@link SysUserDO#getId()}
     */
    private Long userId;
    /**
     * 用户账号
     *
     * 冗余，因为账号可以变更
     */
    private String username;
    /**
     * 登陆结果
     *
     * 枚举 {@link SysLoginResultEnum}
     */
    private Integer result;

    /**
     * 用户 IP
     */
    private String userIp;

    /**
     * 浏览器 UA
     */
    private String userAgent;

}
