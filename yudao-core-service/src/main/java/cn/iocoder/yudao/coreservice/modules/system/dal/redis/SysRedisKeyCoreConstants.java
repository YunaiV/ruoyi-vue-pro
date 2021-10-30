package cn.iocoder.yudao.coreservice.modules.system.dal.redis;

import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import me.zhyd.oauth.model.AuthUser;

import java.time.Duration;

import static cn.iocoder.yudao.framework.redis.core.RedisKeyDefine.KeyTypeEnum.STRING;

/**
 * System Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface SysRedisKeyCoreConstants {

    RedisKeyDefine LOGIN_USER = new RedisKeyDefine("登录用户的缓存",
            "login_user:%s", // 参数为 sessionId
            STRING, LoginUser.class, RedisKeyDefine.TimeoutTypeEnum.DYNAMIC);

    RedisKeyDefine SOCIAL_AUTH_USER = new RedisKeyDefine("社交登陆的授权用户",
            "social_auth_user:%d:%s", // 参数为 type，code
            STRING, AuthUser.class, Duration.ofDays(1));

    RedisKeyDefine SOCIAL_AUTH_STATE = new RedisKeyDefine("社交登陆的 state",
            "social_auth_state:%s", // 参数为 state
            STRING, String.class, Duration.ofHours(24)); // 值为 state
}
