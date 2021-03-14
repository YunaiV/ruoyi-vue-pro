package cn.iocoder.dashboard.modules.system.dal.dataobject.logger;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.modules.system.enums.logger.SysLoginLogTypeEnum;
import cn.iocoder.dashboard.modules.system.enums.logger.SysLoginResultEnum;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 登陆日志表
 *
 * 注意，包括登陆和登出两种行为
 *
 * @author ruoyi
 */
@TableName("sys_login_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SysLoginLogDO extends BaseDO {

    /**
     * 日志主键
     */
    private Long id;
    /**
     * 日志类型
     *
     * 枚举 {@link SysLoginLogTypeEnum}
     */
    private Integer logType;
    /**
     * 链路追踪编号
     */
    private String traceId;
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
