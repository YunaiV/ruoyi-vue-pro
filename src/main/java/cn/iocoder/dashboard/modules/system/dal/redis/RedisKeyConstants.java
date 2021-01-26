package cn.iocoder.dashboard.modules.system.dal.redis;

import cn.iocoder.dashboard.framework.redis.core.RedisKeyDefine;
import cn.iocoder.dashboard.framework.security.core.LoginUser;

import java.time.Duration;

import static cn.iocoder.dashboard.framework.redis.core.RedisKeyDefine.KeyTypeEnum.STRING;

/**
 * Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * {@link LoginUser} 的缓存
     *
     * key 的 format 的参数是 sessionId
     */
    RedisKeyDefine LOGIN_USER = new RedisKeyDefine("login_user:%s", STRING, LoginUser.class,
            Duration.ofMinutes(30));

    /**
     * 验证码的缓存
     *
     * key 的 format 的参数是 uuid
     */
    RedisKeyDefine CAPTCHA_CODE = new RedisKeyDefine("captcha_code:%s", STRING, String.class,
            RedisKeyDefine.TimeoutTypeEnum.DYNAMIC);

}
