package cn.iocoder.yudao.adminserver.modules.system.dal.redis;

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
public interface SysRedisKeyConstants {

    RedisKeyDefine LOGIN_USER = new RedisKeyDefine("登陆用户的缓存",
            "login_user:%s", // 参数为 sessionId
            STRING, LoginUser.class, RedisKeyDefine.TimeoutTypeEnum.DYNAMIC);

    RedisKeyDefine CAPTCHA_CODE = new RedisKeyDefine("验证码的缓存",
            "captcha_code:%s", // 参数为 uuid
            STRING, String.class, RedisKeyDefine.TimeoutTypeEnum.DYNAMIC);

    RedisKeyDefine AUTH_SOCIAL_USER = new RedisKeyDefine("认证的社交用户",
            "auth_social_user:%d:%s", // 参数为 type，code
            STRING, AuthUser.class, Duration.ofDays(1));

}
