package cn.iocoder.yudao.module.pay.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具类
 *
 * @author 芋道源码
 */
public class MoneyUtils {

    /**
     * 计算百分比金额，四舍五入
     *
     * @param price 金额
     * @param rate 百分比，例如说 56.77% 则传入 56.77
     * @return 百分比金额
     */
    public static Integer calculateRatePrice(Integer price, Double rate) {
        return new BigDecimal(price)
                .multiply(BigDecimal.valueOf(rate)) // 乘以
                .setScale(0, RoundingMode.HALF_UP) // 四舍五入
                .intValue();
    }

}
