package cn.iocoder.yudao.module.crm.enums.common;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * CRM 列表检索场景
 *
 * @author HUIHUI
 */
@Getter
@AllArgsConstructor
public enum CrmSceneTypeEnum implements ArrayValuable<Integer> {

    OWNER(1, "我负责的"),
    INVOLVED(2, "我参与的"),
    SUBORDINATE(3, "下属负责的");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CrmSceneTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 场景类型
     */
    private final Integer type;
    /**
     * 场景名称
     */
    private final String name;

    public static boolean isOwner(Integer type) {
        return ObjUtil.equal(OWNER.getType(), type);
    }

    public static boolean isInvolved(Integer type) {
        return ObjUtil.equal(INVOLVED.getType(), type);
    }

    public static boolean isSubordinate(Integer type) {
        return ObjUtil.equal(SUBORDINATE.getType(), type);
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
