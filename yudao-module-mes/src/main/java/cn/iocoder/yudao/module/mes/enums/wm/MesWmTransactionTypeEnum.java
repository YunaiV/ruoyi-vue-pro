package cn.iocoder.yudao.module.mes.enums.wm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 库存事务类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesWmTransactionTypeEnum implements ArrayValuable<Integer> {

    /**
     * 入库
     */
    IN(1, "入库", true),
    /**
     * 出库
     */
    OUT(2, "出库", false),
    /**
     * 调拨移出
     */
    MOVE_OUT(3, "调拨移出", false),
    /**
     * 调拨移入
     */
    MOVE_IN(4, "调拨移入", true),
    /**
     * 盘盈
     */
    ADJUST_IN(5, "盘盈", true),
    /**
     * 盘亏
     */
    ADJUST_OUT(6, "盘亏", false);

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmTransactionTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;
    /**
     * 是否为入库方向
     */
    private final boolean inbound;

    public static MesWmTransactionTypeEnum valueOf(Integer type) {
        return CollUtil.findOne(CollUtil.newArrayList(values()),
                e -> ObjUtil.equal(e.getType(), type));
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
