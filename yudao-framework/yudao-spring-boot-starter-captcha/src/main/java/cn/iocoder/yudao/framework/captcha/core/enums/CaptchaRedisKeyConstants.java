package cn.iocoder.yudao.framework.captcha.core.enums;

import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import com.xingyuv.captcha.model.vo.PointVO;

import java.time.Duration;

import static cn.iocoder.yudao.framework.redis.core.RedisKeyDefine.KeyTypeEnum.STRING;

/**
 * 验证码 Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface CaptchaRedisKeyConstants {

    RedisKeyDefine AJ_CAPTCHA_REQ_LIMIT = new RedisKeyDefine("验证码的请求限流",
            "AJ.CAPTCHA.REQ.LIMIT-%s-%s",
            STRING, Integer.class, Duration.ofSeconds(60)); // 例如说：验证失败 5 次，get 接口锁定

    RedisKeyDefine AJ_CAPTCHA_RUNNING = new RedisKeyDefine("验证码的坐标",
            "RUNNING:CAPTCHA:%s", // AbstractCaptchaService.REDIS_CAPTCHA_KEY
            STRING, PointVO.class, Duration.ofSeconds(120)); // {"secretKey":"PP1w2Frr2KEejD2m","x":162,"y":5}

}
