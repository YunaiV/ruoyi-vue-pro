package cn.iocoder.yudao.module.infra.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 参数配置表
 *
 * @author 芋道源码
 */
@TableName("infra_config")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ConfigDO extends BaseDO {

    /**
     * 参数主键
     */
    @TableId
    private Long id;
    /**
     * 参数分组
     */
    @TableField("`group`")
    private String group;
    /**
     * 参数名称
     */
    private String name;
    /**
     * 参数键名
     */
    @TableField("`key`")
    private String key;
    /**
     * 参数键值
     */
    private String value;
    /**
     * 参数类型
     *
     * 枚举 {@link InfConfigTypeEnum}
     */
    @TableField("`type`")
    private Integer type;
    /**
     * 是否敏感
     *
     * 对于敏感配置，需要管理权限才能查看
     */
    @TableField("`sensitive`")
    private Boolean sensitive;
    /**
     * 备注
     */
    private String remark;

}
