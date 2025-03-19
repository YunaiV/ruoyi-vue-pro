package cn.iocoder.yudao.module.pay.dal.redis;

/**
 * 支付 Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * 通知任务的分布式锁
     *
     * KEY 格式：pay_notify:lock:%d // 参数来自 DefaultLockKeyBuilder 类
     * VALUE 数据格式：HASH // RLock.class：Redisson 的 Lock 锁，使用 Hash 数据结构
     * 过期时间：不固定
     */
    String PAY_NOTIFY_LOCK = "pay_notify:lock:%d";

    /**
     * 支付钱包的分布式锁
     *
     * KEY 格式：pay_wallet:lock:%d
     * VALUE 数据格式：HASH // RLock.class：Redisson 的 Lock 锁，使用 Hash 数据结构
     * 过期时间：不固定
     */
    String PAY_WALLET_LOCK = "pay_wallet:lock:%d";

    /**
     * 支付序号的缓存
     *
     * KEY 格式：pay_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String PAY_NO = "pay_no:";

}
