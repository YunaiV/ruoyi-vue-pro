package cn.iocoder.yudao.module.product.enums.spu;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 产品单位枚举
 *
 * @author HUIHUI
 */
@Getter
@AllArgsConstructor
public enum ProductUnitEnum implements IntArrayValuable {
    PIECE(1, "个"),
    DOZEN(2, "打"),
    BOX(3, "盒"),
    BAG(4, "袋"),
    CASE(5, "箱"),
    SET(6, "套"),
    PACK(7, "包"),
    PAIR(8, "双"),
    ROLL(9, "卷"),
    SHEET(10, "张"),
    WEIGHT(11, "克"),
    KILOGRAM(12, "千克"),
    MILLIGRAM(13, "毫克"),
    MICROGRAM(14, "微克"),
    TON(15, "吨"),
    LITER(16, "升"),
    MILLILITER(17, "毫升"),
    SQUARE_METER(19, "平方米"),
    SQUARE_KILOMETER(20, "平方千米"),
    SQUARE_MILE(21, "平方英里"),
    SQUARE_YARD(22, "平方码"),
    SQUARE_FOOT(23, "平方英尺"),
    CUBIC_METER(24, "立方米"),
    CUBIC_CENTIMETER(25, "立方厘米"),
    CUBIC_INCH(26, "立方英寸"),
    METER(27, "米"),
    CENTIMETER(29, "厘米"),
    MILLIMETER(30, "毫米"),
    INCH(31, "英寸"),
    FOOT(32, "英尺"),
    YARD(33, "码"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ProductUnitEnum::getStatus).toArray();

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
