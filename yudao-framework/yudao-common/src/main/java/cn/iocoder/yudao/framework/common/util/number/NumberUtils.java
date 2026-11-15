package cn.iocoder.yudao.framework.common.util.number;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 数字的工具类，补全 {@link cn.hutool.core.util.NumberUtil} 的功能
 *
 * @author 芋道源码
 */
public class NumberUtils {

    private static final Pattern FIRST_BIG_DECIMAL_PATTERN = Pattern.compile("-?\\d+(?:\\.\\d+)?");

    /**
     * 一百，用于百分数和比例计算
     */
    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public static Long parseLong(String str) {
        return StrUtil.isNotEmpty(str) ? Long.valueOf(str) : null;
    }

    public static Integer parseInt(String str) {
        return StrUtil.isNotEmpty(str) ? Integer.valueOf(str) : null;
    }

    /**
     * 获得数字，为 null 时返回 {@link BigDecimal#ZERO}
     *
     * @param value 数字
     * @return 数字
     */
    public static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 获得数字去掉末尾 0 后的十进制文本（{@link BigDecimal#toPlainString()}）的长度，含负号与小数点
     *
     * 按 precision、scale 计算，不实际展开文本：例如 1E+999999999 展开后约 1GB，这里只返回长度；
     * 长度使用 long，scale 为 int 的上下界时也不会溢出
     *
     * @param value 数字，不能为 null
     * @return 十进制文本的长度，例如 12.3400 为 5（12.34）、-0.05 为 5、0 为 1
     * @throws ArithmeticException 去掉末尾 0 后 scale 超出 int 范围时
     */
    public static long getPlainStringLength(BigDecimal value) {
        BigDecimal number = value.stripTrailingZeros();
        long precision = number.precision();
        long scale = number.scale();
        long length = scale <= 0 ? precision - scale : scale >= precision ? scale + 2 : precision + 1;
        return number.signum() < 0 ? length + 1 : length;
    }

    /**
     * 从文本中解析首个数字
     *
     * @param text 文本
     * @return 数字；不存在时返回 {@link BigDecimal#ZERO}
     */
    public static BigDecimal parseFirstBigDecimal(String text) {
        if (StrUtil.isEmpty(text)) {
            return BigDecimal.ZERO;
        }
        String number = ReUtil.getGroup0(FIRST_BIG_DECIMAL_PATTERN, text.replace(",", ""));
        return NumberUtil.toBigDecimal(number);
    }

    public static boolean isAllNumber(List<String> values) {
        if (CollUtil.isEmpty(values)) {
            return false;
        }
        for (String value : values) {
            if (!NumberUtil.isNumber(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 通过经纬度获取地球上两点之间的距离
     *
     * 参考 <<a href="https://gitee.com/dromara/hutool/blob/1caabb586b1f95aec66a21d039c5695df5e0f4c1/hutool-core/src/main/java/cn/hutool/core/util/DistanceUtil.java">DistanceUtil</a>> 实现，目前它已经被 hutool 删除
     *
     * @param lat1 经度1
     * @param lng1 纬度1
     * @param lat2 经度2
     * @param lng2 纬度2
     * @return 距离，单位：千米
     */
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = lat1 * Math.PI / 180.0;
        double radLat2 = lat2 * Math.PI / 180.0;
        double a = radLat1 - radLat2;
        double b = lng1 * Math.PI / 180.0 - lng2 * Math.PI / 180.0;
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        distance = distance * 6378.137;
        distance = Math.round(distance * 10000d) / 10000d;
        return distance;
    }

    /**
     * 提供精确的乘法运算
     *
     * 和 hutool {@link NumberUtil#mul(BigDecimal...)} 的差别是，如果存在 null，则返回 null
     *
     * @param values 多个被乘值
     * @return 积
     */
    public static BigDecimal mul(BigDecimal... values) {
        for (BigDecimal value : values) {
            if (value == null) {
                return null;
            }
        }
        return NumberUtil.mul(values);
    }

    /**
     * 除法运算，结果保留两位小数并四舍五入
     *
     * @param dividend 被除数，空值按 0 处理
     * @param divisor 除数，为空或 0 时返回 0
     * @return 商
     */
    public static Double div(Long dividend, Long divisor) {
        if (divisor == null || divisor == 0) {
            return 0D;
        }
        return BigDecimal.valueOf(dividend == null ? 0L : dividend)
                .divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

}
