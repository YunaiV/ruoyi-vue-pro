package cn.iocoder.yudao.module.fms.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.ErpFinanceReceiptDO;
import cn.iocoder.yudao.module.fms.dal.redis.FmsRedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;


/**
 * Erp 订单序号的 Redis DAO
 *
 * @author HUIHUI
 */
@Repository
@SuppressWarnings("all")
public class FmsNoRedisDAO {


    /**
     * 付款单 {@link ErpFinancePaymentDO}
     */
    public static final String FINANCE_PAYMENT_NO_PREFIX = "FKD";
    /**
     * 收款单 {@link ErpFinanceReceiptDO}
     */
    public static final String FINANCE_RECEIPT_NO_PREFIX = "SKD";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成序号，使用当前日期，格式为 {PREFIX} + "_" + yyyyMMdd + "_" + 6 位自增
     * 例如说：QTRK-20211009-000001 （没有中间空格）
     *
     * @param prefix 前缀
     * @return 序号
     */
    public String generate(String prefix, ErrorCode message) {
        // 递增序号
        String keyPrefix = prefix + StrPool.DASHED + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        String key = FmsRedisKeyConstants.FMS_NO + keyPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        //判断no是否大于6位数
        ThrowUtil.ifGreater(no, 999999L, message);
        // 设置过期时间
        stringRedisTemplate.expire(key, Duration.ofDays(1L));
        return keyPrefix + StrPool.DASHED + String.format("%06d", no);
    }

}
