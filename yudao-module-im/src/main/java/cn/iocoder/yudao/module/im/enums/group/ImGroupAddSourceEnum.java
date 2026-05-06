package cn.iocoder.yudao.module.im.enums.group;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IM 加入群聊来源枚举
 * <p>
 * 由发起方在申请 / 邀请时传入；同意后同步写入 ImGroupMemberDO 的 addSource 字段
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImGroupAddSourceEnum implements ArrayValuable<Integer> {

    // TODO @AI：没实现的方式，记得在后面加个 TODO @芋艿：未实现，原因。。。。
    SEARCH(1, "搜索"),
    INVITE(2, "邀请"),
    QR_CODE(3, "扫码"),
    SHARE_LINK(4, "分享链接");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImGroupAddSourceEnum::getSource).toArray(Integer[]::new);

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
