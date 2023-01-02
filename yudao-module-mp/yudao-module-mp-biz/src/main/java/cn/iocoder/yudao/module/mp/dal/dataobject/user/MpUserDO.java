package cn.iocoder.yudao.module.mp.dal.dataobject.user;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 微信公众号粉丝 DO
 *
 * @author 芋道源码
 */
@TableName("mp_user")
@KeySequence("mp_user_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpUserDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户标识
     */
    private String openid;
    /**
     * 关注状态
     *
     * 枚举 {@link CommonStatusEnum}
     * 1. 开启 - 已关注
     * 2. 禁用 - 取消关注
     */
    private Integer subscribeStatus;
    /**
     * 关注时间
     */
    private LocalDateTime subscribeTime;
    /**
     * 取消关注时间
     */
    private LocalDateTime unsubscribeTime;
    /**
     * 昵称
     *
     * 注意，2021-12-27 公众号接口不再返回头像和昵称，只能通过微信公众号的网页登录获取
     */
    private String nickname;
    /**
     * 头像地址
     *
     * 注意，2021-12-27 公众号接口不再返回头像和昵称，只能通过微信公众号的网页登录获取
     */
    private String headImageUrl;
    /**
     * 语言
     */
    private String language;
    /**
     * 国家
     */
    private String country;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 备注
     */
    private String remark;

    /**
     * 微信公众号 ID
     *
     * 关联 {@link MpAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 微信公众号 appid
     *
     * 冗余 {@link MpAccountDO#getAppId()}
     */
    private String appId;

}
