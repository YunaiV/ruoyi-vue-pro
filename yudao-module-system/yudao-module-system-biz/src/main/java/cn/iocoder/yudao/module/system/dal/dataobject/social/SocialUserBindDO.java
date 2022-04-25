package cn.iocoder.yudao.module.system.dal.dataobject.social;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 社交用户的绑定
 * 即 {@link SocialUserDO} 与 UserDO 的关联表
 *
 * @author 芋道源码
 */
@TableName(value = "system_social_user_bind", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserBindDO extends BaseDO {

    /**
     * 关联的用户编号
     *
     * 关联 UserDO 的编号
     */
    private Long userId;
    /**
     * 用户类型
     *
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;

    /**
     * 社交平台的用户编号
     *
     * 关联 {@link SocialUserDO#getId()}
     */
    private Long socialUserId;
    /**
     * 社交平台的类型
     *
     * 冗余 {@link SocialUserDO#getType()}
     */
    private Integer socialType;

}
