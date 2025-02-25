package cn.iocoder.yudao.framework.common.util.number;


/**
 * 单位转换工具类
 * 提供毫米 (mm) 与厘米 (cm) 等单位的转换方法
 */
public class LengthUtils {

    private static final float MM_TO_CM_CONVERSION_FACTOR = 0.1F;   // 毫米到厘米转换因子
    private static final float MM_TO_M_CONVERSION_FACTOR = 0.001F; // 毫米到米转换因子

    // === Float 类型转换 ===

    /**
     * 将毫米 (mm) 转换为厘米 (cm) - Float 返回类型
     *
     * @param mmValue 毫米值
     * @return 转换后的厘米值，若输入为 null 则返回 null
     */
    public static Float mmToCmAsFloat(Float mmValue) {
        return mmValue != null ? mmValue * MM_TO_CM_CONVERSION_FACTOR : null;
    }

    /**
     * 将厘米 (cm) 转换为毫米 (mm) - Float 返回类型
     *
     * @param cmValue 厘米值
     * @return 转换后的毫米值，若输入为 null 则返回 null
     */
    public static Float cmToMmAsFloat(Float cmValue) {
        return cmValue != null ? cmValue / MM_TO_CM_CONVERSION_FACTOR : null;
    }

    /**
     * 将毫米 (mm) 转换为米 (m) - Float 返回类型
     *
     * @param mmValue 毫米值
     * @return 转换后的米值，若输入为 null 则返回 null
     */
    public static Float mmToMAsFloat(Float mmValue) {
        return mmValue != null ? mmValue * MM_TO_M_CONVERSION_FACTOR : null;
    }

    /**
     * 将米 (m) 转换为毫米 (mm) - Float 返回类型
     *
     * @param mValue 米值
     * @return 转换后的毫米值，若输入为 null 则返回 null
     */
    public static Float mToMmAsFloat(Float mValue) {
        return mValue != null ? mValue / MM_TO_M_CONVERSION_FACTOR : null;
    }

    /**
     * 将毫米 (mm) 转换为厘米 (cm) - Float 返回类型 (Integer 输入)
     *
     * @param mmValue 毫米值
     * @return 转换后的厘米值，若输入为 null 则返回 null
     */
    public static Float mmToCmAsFloat(Integer mmValue) {
        return mmValue != null ? mmValue * MM_TO_CM_CONVERSION_FACTOR : null;
    }

    // === Double 类型转换 ===

    /**
     * 将毫米 (mm) 转换为厘米 (cm) - Double 返回类型
     *
     * @param mmValue 毫米值
     * @return 转换后的厘米值，若输入为 null 则返回 null
     */
    public static Double mmToCmAsDouble(Double mmValue) {
        return mmValue != null ? mmValue * MM_TO_CM_CONVERSION_FACTOR : null;
    }

    /**
     * 将厘米 (cm) 转换为毫米 (mm) - Double 返回类型
     *
     * @param cmValue 厘米值
     * @return 转换后的毫米值，若输入为 null 则返回 null
     */
    public static Double cmToMmAsDouble(Double cmValue) {
        return cmValue != null ? cmValue / MM_TO_CM_CONVERSION_FACTOR : null;
    }

    /**
     * 将毫米 (mm) 转换为米 (m) - Double 返回类型
     *
     * @param mmValue 毫米值
     * @return 转换后的米值，若输入为 null 则返回 null
     */
    public static Double mmToMAsDouble(Double mmValue) {
        return mmValue != null ? mmValue * MM_TO_M_CONVERSION_FACTOR : null;
    }

    /**
     * 将米 (m) 转换为毫米 (mm) - Double 返回类型
     *
     * @param mValue 米值
     * @return 转换后的毫米值，若输入为 null 则返回 null
     */
    public static Double mToMmAsDouble(Double mValue) {
        return mValue != null ? mValue / MM_TO_M_CONVERSION_FACTOR : null;
    }


}