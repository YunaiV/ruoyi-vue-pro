package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 条码格式枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BarcodeFormatEnum implements ArrayValuable<Integer> {

    QR_CODE(1, "二维码"),
    EAN13(2, "EAN13 商品条码"),
    CODE39(3, "CODE39 工业条码"),
    UPC_A(4, "UPC-A 美国商品码");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BarcodeFormatEnum::getValue).toArray(Integer[]::new);

    /**
     * 格式值
     */
    private final Integer value;
    /**
     * 格式名称
     */
    private final String label;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
