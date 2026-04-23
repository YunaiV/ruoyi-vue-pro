package cn.iocoder.yudao.module.im.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * IM 错误码枚举类
 * <p>
 * im 系统，使用 1-040-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 消息 (1-040-300-000) ==========
    ErrorCode MESSAGE_NOT_EXISTS = new ErrorCode(1_040_300_000, "消息不存在");
    ErrorCode MESSAGE_RECALL_DENIED = new ErrorCode(1_040_300_002, "只能撤回自己发送的消息");
    ErrorCode MESSAGE_ALREADY_RECALLED = new ErrorCode(1_040_300_003, "消息已撤回");
    ErrorCode MESSAGE_SENSITIVE_WORD_BLOCKED = new ErrorCode(1_040_300_004, "消息包含敏感词，无法发送");
    ErrorCode MESSAGE_PULL_SIZE_EXCEEDED = new ErrorCode(1_040_300_005, "单次拉取消息数量不能超过 {} 条");
    ErrorCode MESSAGE_RECALL_TIMEOUT = new ErrorCode(1_040_300_007, "超过 {} 分钟的消息无法撤回");

    // ========== 群 (1-040-400-000) ==========
    ErrorCode GROUP_NOT_EXISTS = new ErrorCode(1_040_400_000, "群不存在");
    ErrorCode GROUP_BANNED = new ErrorCode(1_040_400_001, "群已被封禁");
    ErrorCode GROUP_DISSOLVED = new ErrorCode(1_040_400_002, "群已解散");
    ErrorCode GROUP_NOT_OWNER = new ErrorCode(1_040_400_003, "仅群主可执行该操作");

    // ========== 群成员 (1-040-500-000) ==========
    ErrorCode GROUP_MEMBER_NOT_IN_GROUP = new ErrorCode(1_040_500_001, "您已不在该群中");
    ErrorCode GROUP_OWNER_CANNOT_QUIT = new ErrorCode(1_040_500_003, "群主不能退出群聊，请先转让群主或解散群聊");
    ErrorCode GROUP_CANNOT_REMOVE_SELF = new ErrorCode(1_040_500_004, "不能将自己移出群聊");
    ErrorCode GROUP_MEMBER_EXCEED = new ErrorCode(1_040_500_005, "群聊人数不能超过 {} 人");
    ErrorCode GROUP_INVITE_NOT_FRIEND = new ErrorCode(1_040_500_006, "'{}' 不是您的好友，邀请失败");

    // ========== 好友 (1-040-600-000) ==========
    ErrorCode FRIEND_NOT_FRIEND = new ErrorCode(1_040_600_001, "对方不是您的好友");
    ErrorCode FRIEND_ADD_SELF = new ErrorCode(1_040_600_002, "不允许添加自己为好友");

}
