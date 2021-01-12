package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型表
 *
 * @author ruoyi
 */
@TableName("sys_dict_type")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictTypeDO extends BaseDO {

    /**
     * 字典主键
     */
    @TableId
    private Long id;
    /**
     * 字典名称
     */
    private String name;
    /**
     * 字典类型
     */
    @TableField("dict_type")
    private String type;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
