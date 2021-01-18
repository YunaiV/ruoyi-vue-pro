package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.config;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.modules.system.enums.config.SysConfigTypeEnum;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 参数配置表
 *
 * @author ruoyi
 */
@TableName("sys_config")
public class SysConfigDO extends BaseDO {

    /**
     * 参数主键
     */
    private Long id;
    /**
     * 参数分组
     */
    private String group;
    /**
     * 参数名称
     */
    private String name;
    /**
     * 参数键名
     */
    private String key;
    /**
     * 参数键值
     */
    private String value;
    /**
     * 参数类型
     *
     * 枚举 {@link SysConfigTypeEnum}
     */
    private String type;
    /**
     * 是否敏感
     *
     * 对于敏感配置，需要管理权限才能查看
     */
    private Boolean sensitive;
    /**
     * 备注
     */
    private String remark;

}
