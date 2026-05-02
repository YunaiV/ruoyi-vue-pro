package cn.iocoder.yudao.module.im.enums.message;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IM 消息类型枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum ImMessageTypeEnum implements ArrayValuable<Integer> {

    // ========== 用户聊天消息 ==========
    TEXT(0, "文本", true, true), // 对应 TextMessage 类
    IMAGE(1, "图片", true, true), // 对应 ImageMessage 类
    FILE(2, "文件", true, true), // 对应 FileMessage 类
    VOICE(3, "语音", true, true), // 对应 AudioMessage 类
    VIDEO(4, "视频", true, true), // 对应 VideoMessage 类

    // ========== 信号类 ==========
    RECALL(10, "撤回", true, false), // 对应 RecallMessage 类
    READ(11, "已读", false, false), // 暂无
    RECEIPT(12, "回执", false, false), // 暂无

    // ========== 系统提示 ==========
    TIP_TIME(20, "时间分隔", false, false), // 暂无（前端本地生成）
    TIP_TEXT(21, "系统提示", true, false), // 对应 TextMessage 类

    // ========== 好友变更 ==========
    FRIEND_ADD(100, "好友添加", false, false), // 暂无
    FRIEND_DELETE(101, "好友删除", false, false), // 暂无
    FRIEND_UPDATE(102, "好友更新", false, false), // 暂无（客户端收到后自行拉取）

    // ========== 群变更 ==========
    GROUP_CREATE(200, "群创建", false, false), // 暂无
    GROUP_UPDATE(201, "群信息变更", false, false), // 暂无
    GROUP_DELETE(202, "群删除", false, false), // 暂无（解散/退群/踢出均用此类型）
    GROUP_MEMBER_UPDATE(203, "群成员信息变更", false, false); // 暂无（客户端收到后自行拉取）

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImMessageTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;
    /**
     * 是否入库
     * <p>
     * true：插入 im_xxx_message 消息表，离线 pull 能拉到；
     * false：仅 WebSocket 推送，离线丢弃；状态由专用存储维护（如 READ 走 Redis 游标）
     */
    private final boolean persistent;
    /**
     * 是不是用户聊天消息（normal vs event 二分）
     * <p>
     * true：用户主动发的聊天消息，计入会话未读数（接收方非激活会话时 unreadCount + 1）；用户发送入口仅允许这类；
     * false：系统事件 / 信号 / 提示，不参与未读计数
     */
    private final boolean normal;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 校验 type 已注册，并返回对应枚举；未注册立刻抛异常，避免新增 type 时漏配 persistent / normal 属性
     *
     * @param type 消息类型
     * @return 枚举实例
     */
    public static ImMessageTypeEnum validate(Integer type) {
        ImMessageTypeEnum result = ArrayUtil.firstMatch(item -> item.type.equals(type), values());
        Assert.notNull(result, "未注册的消息类型 type={}", type);
        return result;
    }

}
