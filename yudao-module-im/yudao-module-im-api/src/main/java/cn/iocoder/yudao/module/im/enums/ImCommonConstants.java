package cn.iocoder.yudao.module.im.enums;

/**
 * IM 通用常量
 *
 * @author 芋道源码
 */
public interface ImCommonConstants {

    /**
     * pull 最大拉取数量
     */
    int MESSAGE_MAX_PULL_SIZE = 1000;

    /**
     * 消息撤回时间限制（分钟）
     */
    int MESSAGE_RECALL_TIMEOUT_MINUTES = 5;

    /**
     * 私聊离线消息最大拉取天数
     * <p>
     * 客户端通过 pull 接口增量拉取私聊离线消息时，仅返回最近 N 天内产生的消息，
     * 超过该窗口的老消息不再主动推送（可通过历史消息接口按需倒翻）。
     */
    int MESSAGE_PRIVATE_PULL_MAX_DAYS = 30;

    /**
     * 群聊离线消息最大拉取天数
     * <p>
     * 客户端通过 pull 接口增量拉取群聊离线消息时，仅返回最近 N 天内产生的消息；
     * 退群前消息的补齐也以该窗口为基准（早于窗口的退群群不再扫描），避免老用户首次
     * 拉取时对历史退群群做大量查询。
     */
    int MESSAGE_GROUP_PULL_MAX_DAYS = 30;

    // ==================== 好友相关提示消息 ====================

    /**
     * 好友添加系统提示
     */
    String FRIEND_ADD_TIP_MESSAGE = "你们已成为好友，现在可以开始聊天了";
    /**
     * 好友删除系统提示
     */
    String FRIEND_DELETE_TIP_MESSAGE = "你们的好友关系已被解除";

    // ==================== 群相关提示消息 ====================

    /**
     * 群解散系统提示
     */
    String GROUP_DISSOLVE_TIP_MESSAGE = "'{}'解散了群聊";
    /**
     * 退群系统提示
     */
    String GROUP_QUIT_TIP_MESSAGE = "您已退出群聊";
    /**
     * 踢出群系统提示
     */
    String GROUP_REMOVE_TIP_MESSAGE = "您已被移出群聊";

}
