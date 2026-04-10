package cn.iocoder.yudao.module.mes.enums.md;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MES 物料产品分类 - 物料/产品标识枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesMdItemTypeEnum {

    /**
     * 物料
     */
    ITEM("ITEM", "物料"),
    /**
     * 产品
     */
    PRODUCT("PRODUCT", "产品");

    /**
     * 标识值
     */
    private final String value;
    /**
     * 标识名
     */
    private final String name;

    /**
     * 判断给定值是否为产品类型
     */
    public static boolean isProduct(String value) {
        return PRODUCT.getValue().equals(value);
    }

}
