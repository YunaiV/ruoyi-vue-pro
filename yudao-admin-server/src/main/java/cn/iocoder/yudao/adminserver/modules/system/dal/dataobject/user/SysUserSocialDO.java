package cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.user;

import cn.iocoder.yudao.adminserver.modules.system.enums.user.SysUserSocialTypeEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 三方登陆信息
 * 通过 {@link SysUserSocialDO#getUserId()} 关联到对应的 {@link SysUserDO}
 *
 * @author weir
 */
@TableName(value = "sys_user_social", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserSocialDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 用户类型
     *
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;

    /**
     * 三方平台的类型
     */
    private SysUserSocialTypeEnum type;
    /**
     * 三方 openid
     */
    private String openid;
    /**
     * 三方 token
     */
    private String token;
    /**
     * 三方的全局编号
     *
     * 例如说，微信平台的 https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/union-id.html
     * 如果没有 unionId 的平台，直接使用 openid 作为该字段的值
     */
    private String unionId;

    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 用户头像
     */
    private String avatar;
    /**
     * 原始数据，一般是 JSON 格式
     */
    private String info;

}


