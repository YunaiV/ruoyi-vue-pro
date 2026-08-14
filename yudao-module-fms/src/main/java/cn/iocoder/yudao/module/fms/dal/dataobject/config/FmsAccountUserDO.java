package cn.iocoder.yudao.module.fms.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * FMS 账套用户 DO
 *
 * @author 芋道源码
 */
@TableName("fms_account_user")
@KeySequence("fms_account_user_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsAccountUserDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;
    /**
     * 后台用户编号
     *
     * 关联 {@link cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO#getId()}
     */
    private Long userId;
    /**
     * 是否默认账套
     */
    private Boolean defaultStatus;
    /**
     * 是否账套创建人
     */
    private Boolean founder;
    /**
     * 成员权限级别
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.config.FmsAccountUserLevelEnum}
     */
    private Integer level;
}
