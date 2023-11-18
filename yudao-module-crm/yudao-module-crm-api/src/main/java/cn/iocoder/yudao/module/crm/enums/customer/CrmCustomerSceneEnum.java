package cn.iocoder.yudao.module.crm.enums.customer;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// TODO @puhui999：这个应该是 crm 全局的，不仅仅属于 customer 客户哈；
/**
 * CRM 客户等级
 *
 * @author Wanwan
 */
@Getter
@AllArgsConstructor
public enum CrmCustomerSceneEnum implements IntArrayValuable {

    OWNER(1, "我负责的客户"),
    FOLLOW(2, "我关注的客户");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmCustomerSceneEnum::getType).toArray();

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

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
