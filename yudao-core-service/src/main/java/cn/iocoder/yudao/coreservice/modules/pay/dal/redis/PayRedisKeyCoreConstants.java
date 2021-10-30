package cn.iocoder.yudao.coreservice.modules.pay.dal.redis;

import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import org.redisson.api.RLock;

import static cn.iocoder.yudao.framework.redis.core.RedisKeyDefine.KeyTypeEnum.HASH;

/**
 * Lock4j Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface PayRedisKeyCoreConstants {

    RedisKeyDefine PAY_NOTIFY_LOCK = new RedisKeyDefine("通知任务的分布式锁",
            "pay_notify:lock:", // 参数来自 DefaultLockKeyBuilder 类
            HASH, RLock.class, RedisKeyDefine.TimeoutTypeEnum.DYNAMIC); // Redisson 的 Lock 锁，使用 Hash 数据结构

}
