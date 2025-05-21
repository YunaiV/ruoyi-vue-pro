package cn.iocoder.yudao.module.trade.dal.redis;

/**
 * 交易 Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * 交易序号的缓存
     *
     * KEY 格式：trade_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String TRADE_NO = "trade_no:";

    /**
     * 交易序号的缓存
     *
     * KEY 格式：express_track:{code-logisticsNo-receiverMobile}
     * VALUE 数据格式 String, 物流信息集合
     */
    String EXPRESS_TRACK = "express_track";

}
