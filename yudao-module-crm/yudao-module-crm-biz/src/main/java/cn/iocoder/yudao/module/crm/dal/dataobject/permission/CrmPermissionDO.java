package cn.iocoder.yudao.module.crm.dal.dataobject.permission;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Crm 数据权限 DO
 *
 * @author HUIHUI
 */
@TableName("crm_permission")
@KeySequence("crm_permission_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
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
     * 数据类型，关联 {@link CrmBizTypeEnum}
     */
    private Integer bizType;
    /**
     * 数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     */
    private Long bizId;
    /**
     * 团队成员，关联 AdminUser#id
     */
    private Long userId;
    /**
     * 权限级别，关联 {@link CrmPermissionLevelEnum}
     * 如果为公海数据的话会干掉此数据的负责人，领取人则上位负责人
     * 例：客户放入公海后会干掉团队成员中的负责人，而其他团队成员则不受影响
     */
    private Integer permissionLevel;

}
