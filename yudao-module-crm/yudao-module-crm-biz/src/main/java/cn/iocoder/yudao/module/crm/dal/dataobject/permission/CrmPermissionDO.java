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

    // TODO puhui999：是不是公海的数据，就不插入了；这样方便获取公海数据鸭
    // TODO @puhui999：每个数据那的负责人，我想了下，还是存储的；
    /**
     * 当数据变为公海数据时，也就是数据团队成员中没有负责人的时候，将原本的负责人 userId 设置为 POOL_USER_ID 方便查询公海数据。
     * 也就是说每条数据到最后都有一个负责人，如果有人领取则 userId 为领取人
     */
    public static final Long POOL_USER_ID = 0L;

    /**
     * ID
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
     * 团队成员
     *
     * 关联 AdminUser 的 id 字段
     *
     * 如果为公海数据的话会干掉此数据的负责人后设置为 {@link #POOL_USER_ID}，领取人则上位负责人
     * 例：客户放入公海后会干掉团队成员中的负责人，而其他团队成员则不受影响
     */
    private Long userId;

    /**
     * 权限级别
     *
     * 关联 {@link CrmPermissionLevelEnum}
     */
    private Integer level;

}
