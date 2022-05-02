package cn.iocoder.yudao.module.system.dal.redis;

import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import cn.iocoder.yudao.framework.security.core.LoginUser;

import java.time.Duration;

import static cn.iocoder.yudao.framework.redis.core.RedisKeyDefine.KeyTypeEnum.STRING;

/**
 * System Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    RedisKeyDefine CAPTCHA_CODE = new RedisKeyDefine("验证码的缓存",
            "captcha_code:%s", // 参数为 uuid
            STRING, String.class, RedisKeyDefine.TimeoutTypeEnum.DYNAMIC);

    RedisKeyDefine LOGIN_USER = new RedisKeyDefine("登录用户的缓存",
            "login_user:%s", // 参数为 token 令牌
            STRING, LoginUser.class, RedisKeyDefine.TimeoutTypeEnum.DYNAMIC);

    RedisKeyDefine SOCIAL_AUTH_STATE = new RedisKeyDefine("社交登陆的 state", // 注意，它是被 JustAuth 的 justauth.type.prefix 使用到
            "social_auth_state:%s", // 参数为 state
            STRING, String.class, Duration.ofHours(24)); // 值为 state

}
