package cn.iocoder.yudao.module.mp.dal.dataobject.account;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import lombok.*;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;

/**
 * 公众号账户 DO
 *
 * @author 芋道源码
 */
@TableName("mp_account")
@KeySequence("mp_account_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpAccountDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 公众号名称
     */
    private String name;
    /**
     * 公众号账户
     */
    private String account;
    /**
     * 公众号 appid
     */
    private String appId;
    /**
     * 公众号密钥
     */
    private String appSecret;
    /**
     * 公众号token
     */
    private String token;
    /**
     * 加密密钥
     */
    private String aesKey;
    /**
     * 二维码图片 URL
     */
    private String qrCodeUrl;
    /**
     * 备注
     */
    private String remark;

    // TODO 芋艿：需要迁移走
    public WxMpConfigStorage toWxMpConfigStorage(RedisTemplateWxRedisOps redisTemplateWxRedisOps, WxMpProperties wxMpProperties) {
        WxMpRedisConfigImpl wxMpRedisConfig = new WxMpRedisConfigImpl(redisTemplateWxRedisOps, wxMpProperties.getConfigStorage().getKeyPrefix());
        wxMpRedisConfig.setAppId(appId);
        wxMpRedisConfig.setSecret(appSecret);
        wxMpRedisConfig.setToken(token);
        wxMpRedisConfig.setAesKey(aesKey);
        return wxMpRedisConfig;
    }
}
