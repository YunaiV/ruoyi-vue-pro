package cn.iocoder.yudao.module.product.enums.spu;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// TODO @puhui999：这种非关键的枚举，要不直接写在 ProductSpuPageReqVO 里。类似 public static final Integer TAB_TYPE_FOR_SALE = 0; // 出售中商品
/**
 * 商品 spu Tabs 标签枚举类型
 *
 * @author HUIHUI
 */
@Getter
@AllArgsConstructor
public enum ProductSpuPageTabEnum implements IntArrayValuable {

    FOR_SALE(0,"出售中商品"),
    IN_WAREHOUSE(1,"仓库中商品"),
    SOLD_OUT(2,"已售空商品"),
    ALERT_STOCK(3,"警戒库存"),
    RECYCLE_BIN(4,"商品回收站");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ProductSpuPageTabEnum::getType).toArray();
    /**
     * 状态
     */
    private final Integer type;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
