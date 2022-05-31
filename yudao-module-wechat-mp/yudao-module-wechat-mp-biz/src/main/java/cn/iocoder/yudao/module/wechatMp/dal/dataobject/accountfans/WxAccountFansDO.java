package cn.iocoder.yudao.module.wechatMp.dal.dataobject.accountfans;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 微信公众号粉丝 DO
 *
 * @author 芋道源码
 */
@TableName("wx_account_fans")
@KeySequence("wx_account_fans_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxAccountFansDO extends BaseDO {

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
     * 订阅状态，0未关注，1已关注
     */
    private String subscribeStatus;
    /**
     * 订阅时间
     */
    private Date subscribeTime;
    /**
     * 昵称
     */
    private byte[] nickname;
    /**
     * 性别，1男，2女，0未知
     */
    private String gender;
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
     * 头像地址
     */
    private String headimgUrl;
    /**
     * 备注
     */
    private String remark;
    /**
     * 微信公众号ID
     */
    private String wxAccountId;
    /**
     * 微信公众号appid
     */
    private String wxAccountAppid;

}
