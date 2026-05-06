package cn.iocoder.yudao.module.im.enums.group;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

// TODO @AI：这里是不是对齐微信；申请只有需要审批，和不需要审批。（不然感觉有点复杂、难理解？！）
/**
 * IM 群加群方式枚举
 * <p>
 * 控制「外部用户主动申请」和「群成员邀请」两条入群路径是否需要群主 / 管理员审批
 * <ul>
 *     <li>{@link #FREE}：用户搜到群可直接进群；任何成员邀请也直进</li>
 *     <li>{@link #APPLY}：用户申请进群需群主 / 管理员审批；任何成员邀请仍直进</li>
 *     <li>{@link #APPLY_AND_NORMAL_INVITE}：用户申请需审批；普通成员邀请也需审批；群主 / 管理员邀请直进</li>
 * </ul>
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImGroupJoinTypeEnum implements ArrayValuable<Integer> {

    FREE(0, "自由进群"),
    APPLY(1, "申请需审批"),
    APPLY_AND_NORMAL_INVITE(2, "申请、及普通成员邀请均需审批");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImGroupJoinTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 是否「自由进群」；自由进群路径无需创建申请记录
     */
    public static boolean isFree(Integer type) {
        return Objects.equals(FREE.type, type);
    }

    /**
     * 普通成员邀请是否需要审批
     */
    public static boolean isInviteNeedApproval(Integer type) {
        return Objects.equals(APPLY_AND_NORMAL_INVITE.type, type);
    }

}
