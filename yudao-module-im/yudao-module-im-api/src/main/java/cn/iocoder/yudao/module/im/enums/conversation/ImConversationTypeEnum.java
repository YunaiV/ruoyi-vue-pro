package cn.iocoder.yudao.module.im.enums.conversation;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// TODO @AI：还需要这个枚举么？
/**
 * IM 会话类型枚举
 * 参考 <a href="https://doc.rentsoft.cn/zh-Hans/sdks/enum/conversationType">“会话类型”</a> 文档
 *
 * @author anhaohao
 */
@Getter
@AllArgsConstructor
public enum ImConversationTypeEnum implements ArrayValuable<Integer> {

    SINGLE(1, "单聊"),
    GROUP(3, "群聊"),
    NOTIFICATION(4, "通知会话");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImConversationTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 名字
     */
    private final String name;

    /**
     * 生成会话编号
     *
     * @param fromUserId       发送者编号
     * @param receiverId       接收者编号
     * @param conversationType 会话类型
     * @return 会话编号
     */
    public static String generateConversationNo(Long fromUserId, Long receiverId, Integer conversationType) {
        final String SINGLE_PREFIX = "s_";
        final String GROUP_PREFIX = "g_";

        if (ImConversationTypeEnum.SINGLE.getType().equals(conversationType)) {
            long minId = Math.min(fromUserId, receiverId);
            long maxId = Math.max(fromUserId, receiverId);
            return SINGLE_PREFIX + minId + "_" + maxId;

        } else if (ImConversationTypeEnum.GROUP.getType().equals(conversationType)) {
            return GROUP_PREFIX + receiverId;
        }

        return null;
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}