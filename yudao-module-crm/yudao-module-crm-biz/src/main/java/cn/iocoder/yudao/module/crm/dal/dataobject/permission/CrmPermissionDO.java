package cn.iocoder.yudao.module.crm.dal.dataobject.permission;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * CRM 数据权限 DO
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
     * 编号，主键自增
     */
    @TableId
    private Long id;

    /**
     * 数据类型
     *
     * 枚举 {@link CrmBizTypeEnum}
     */
    private Integer bizType;
    /**
     * 数据编号
     *
     * 关联 {@link CrmBizTypeEnum} 对应模块 DO 的 id 字段
     */
    private Long bizId;

    /**
     * 用户编号
     *
     * 关联 AdminUser 的 id 字段
     */
    private Long userId;

    /**
     * 权限级别
     *
     * 关联 {@link CrmPermissionLevelEnum}
     */
    private Integer level;

}
