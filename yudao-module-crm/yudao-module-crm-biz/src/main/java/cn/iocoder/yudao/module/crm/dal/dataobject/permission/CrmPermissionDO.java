package cn.iocoder.yudao.module.crm.dal.dataobject.permission;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.JsonLongSetTypeHandler;
import cn.iocoder.yudao.module.crm.framework.enums.CrmEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Set;

/**
 * crm 数据权限 DO
 *
 * @author HUIHUI
 */
@TableName("crm_receivable")
@KeySequence("crm_receivable_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmPermissionDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 数据类型 关联 {@link CrmEnum}
     */
    private Integer crmType;
    /**
     * 数据编号 关联 {@link CrmEnum} 对应模块 DO#getId()
     */
    private Long crmDataId;
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
