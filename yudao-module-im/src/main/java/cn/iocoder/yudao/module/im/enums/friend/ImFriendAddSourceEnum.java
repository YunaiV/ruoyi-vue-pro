package cn.iocoder.yudao.module.im.enums.friend;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IM 好友添加来源枚举
 * <p>
 * 由发起方调用 apply 接口时传入；同意后同步写入 im_friend.add_source（双向）
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImFriendAddSourceEnum implements ArrayValuable<Integer> {

    SEARCH(1, "搜索"), // FriendAddDialog 搜索流程
    GROUP(2, "群聊"), // 群成员主页 → UserInfo「加为好友」入口
    QR_CODE(3, "扫码"), // TODO @芋艿：后续实现扫码加好友
    CARD(4, "名片"); // TODO @芋艿：后续实现通过名片加好友，类似微信的「扫一扫 - 名片」功能，或者「通讯录 - 推荐好友」功能

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImFriendAddSourceEnum::getSource).toArray(Integer[]::new);

    /**
     * 来源
     */
    private final Integer source;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
