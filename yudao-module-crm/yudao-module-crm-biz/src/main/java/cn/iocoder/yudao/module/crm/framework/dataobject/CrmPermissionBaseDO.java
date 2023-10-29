package cn.iocoder.yudao.module.crm.framework.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.JsonLongSetTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * crm 数据权限基础实体对象
 *
 * @author HUIHUI
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmPermissionBaseDO extends BaseDO {

    /**
     * 负责人的用户编号 关联 AdminUser#id
     */
    private Long ownerUserId;
    /**
     * 只读权限的用户编号数组
     */
    @TableField(typeHandler = JsonLongSetTypeHandler.class)
    private Set<Long> roUserIds;
    /**
     * 读写权限的用户编号数组
     */
    @TableField(typeHandler = JsonLongSetTypeHandler.class)
    private Set<Long> rwUserIds;

}
