package cn.iocoder.yudao.module.im.enums.message;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// TODO @AI：还需要这个枚举么？
/**
 * IM 消息的类型枚举
 * <p>
 * 参考 <a href="https://doc.rentsoft.cn/zh-Hans/sdks/enum/messageContentType">“消息类型”</a> 文档
 *
 * @author anhaohao
 */
@Getter
@AllArgsConstructor
public enum ImMessageContentTypeEnum implements ArrayValuable<Integer> {

    TEXT(101, "文本消息"),
    PICTURE(102, "图片消息"),
    VOICE(103, "语音消息"),
    VIDEO(104, "视频消息"),
    FILE(105, "文件消息"),
    AT_TEXT(106, "@消息"),
    MERGE(107, "合并消息"),
    CARD(108, "名片消息"),
    LOCATION(109, "位置消息"),
    CUSTOM(110, "自定义消息"),
    REVOKE_RECEIPT(111, "撤回消息回执"),
    C2C_RECEIPT(112, "单聊消息回执"),
    TYPING(113, "输入状态"),
    QUOTE(114, "引用消息"),
    FACE(115, "表情消息"),
    ADVANCED_REVOKE(118, "高级撤回消息"),

    // ========== 好友通知 1200-1299 ===========
    FRIEND_ADDED(1201, "双方成为好友通知"),

    // ========== 系统通知 1400 ==========
    OA_NOTIFICATION(1400, "系统通知"),

    // ========== 群相关 1500-1599 ==========
    GROUP_CREATED(1501, "群创建通知"),
    GROUP_INFO_CHANGED(1502, "群信息改变通知"),
    MEMBER_QUIT(1504, "群成员退出通知"),
    GROUP_OWNER_CHANGED(1507, "群主更换通知"),
    MEMBER_KICKED(1508, "群成员被踢通知"),
    MEMBER_INVITED(1509, "邀请群成员通知"),
    MEMBER_ENTER(1510, "群成员进群通知"),
    GROUP_DISMISSED(1511, "解散群通知"),
    GROUP_MEMBER_MUTED(1512, "群成员禁言通知"),
    GROUP_MEMBER_CANCEL_MUTED(1513, "取消群成员禁言通知"),
    GROUP_MUTED(1514, "群禁言通知"),
    GROUP_CANCEL_MUTED(1515, "取消群禁言通知"),
    GROUP_ANNOUNCEMENT_UPDATED(1519, "群公告改变通知"),
    GROUP_NAME_UPDATED(1520, "群名称改变通知"),

    // TODO 芋艿：其它
    BURN_CHANGE(1701, "阅后即焚开启或关闭通知"),
    REVOKE(2101, "撤回消息通知");;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImMessageContentTypeEnum::getType).toArray(Integer[]::new);

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

}