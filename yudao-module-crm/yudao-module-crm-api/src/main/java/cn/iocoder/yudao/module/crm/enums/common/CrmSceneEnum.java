package cn.iocoder.yudao.module.crm.enums.common;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// TODO @puhui999：这个枚举，要不改成 CrmSceneTypeEnum
/**
 * CRM 列表检索场景
 *
 * @author HUIHUI
 */
@Getter
@AllArgsConstructor
public enum CrmSceneEnum implements IntArrayValuable {

    OWNER(1, "我负责的"),
    FOLLOW(2, "我关注的"),
    // TODO @puhui999：还有一个我参与的
    SUBORDINATE(3, "下属负责的");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmSceneEnum::getType).toArray();

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

    public static boolean isFollow(Integer type) {
        return ObjUtil.equal(FOLLOW.getType(), type);
    }

    public static boolean isSubordinate(Integer type) {
        return ObjUtil.equal(SUBORDINATE.getType(), type);
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
