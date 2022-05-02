package cn.iocoder.yudao.module.pay.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 支付相关编号的生产
 */
// TODO @jason：需要改造，基于 db；
public class PaySeqUtils {

    private static final AtomicLong REFUND_REQ_NO_SEQ = new AtomicLong(0L);

    private static final AtomicLong MER_REFUND_NO_SEQ = new AtomicLong(0L);

    private static final AtomicLong MER_ORDER_NO_SEQ = new AtomicLong(0L);

    /**
     * 生成商户退款单号，用于测试，应该由商户系统生成
     * @return 商户退款单
     */
    public static String genMerchantRefundNo() {
        return String.format("%s%s%04d", "MR",
                DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN),
                (int) MER_REFUND_NO_SEQ.getAndIncrement() % 10000);
    }

    /**
     * 生成退款请求号
     * @return 退款请求号
     */
    public static String genRefundReqNo() {
        return String.format("%s%s%04d", "RR",
                DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN),
                (int) REFUND_REQ_NO_SEQ.getAndIncrement() % 10000);
    }

    /**
     * 生成商户订单编号号 用于测试，应该由商户系统生成
     * @return 商户订单编号
     */
    public static String genMerchantOrderNo() {
        return String.format("%s%s%04d", "MO",
                DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN),
                (int) MER_ORDER_NO_SEQ.getAndIncrement() % 10000);
    }

}
