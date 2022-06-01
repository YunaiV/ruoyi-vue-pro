package cn.iocoder.yudao.module.mp.dal.dataobject.accountfans;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Date;

/**
 * 微信公众号粉丝 DO
 *
 * @author 芋道源码
 */
// TODO @亚洲：WxUserDO
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
    // TODO @亚洲：Integer 然后写个枚举哈
    private String subscribeStatus;
    /**
     * 订阅时间
     */
    // TODO @亚洲：增加一个取消关注的事件
    private Date subscribeTime;
    /**
     * 昵称
     */
    // TODO @亚洲：String
    private byte[] nickname;
    /**
     * 性别，1男，2女，0未知
     */
    // TODO @亚洲：Integer
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
    // TODO @亚洲：headImageUrl
    private String headimgUrl;
    /**
     * 备注
     */
    private String remark;

    // TODO @亚洲：是不是只要存储 WxAccountDO 的 id 呀？
    /**
     * 微信公众号 ID
     */
    private String wxAccountId;
    /**
     * 微信公众号 appid
     */
    private String wxAccountAppid;

}
