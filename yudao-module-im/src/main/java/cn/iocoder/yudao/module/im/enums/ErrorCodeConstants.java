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
    ErrorCode MESSAGE_QUOTE_INVALID = new ErrorCode(1_040_300_008, "引用的消息不可用");
    ErrorCode MESSAGE_NOT_IN_GROUP = new ErrorCode(1_040_300_009, "消息不属于该群");

    // ========== 群 (1-040-400-000) ==========
    ErrorCode GROUP_NOT_EXISTS = new ErrorCode(1_040_400_000, "群不存在");
    ErrorCode GROUP_BANNED = new ErrorCode(1_040_400_001, "群已被封禁");
    ErrorCode GROUP_DISSOLVED = new ErrorCode(1_040_400_002, "群已解散");
    ErrorCode GROUP_NOT_OWNER = new ErrorCode(1_040_400_003, "仅群主可执行该操作");
    ErrorCode GROUP_NOT_OWNER_OR_ADMIN = new ErrorCode(1_040_400_004, "仅群主或管理员可执行该操作");
    ErrorCode GROUP_TRANSFER_OWNER_TO_SELF = new ErrorCode(1_040_400_005, "不能将群主转让给自己");
    ErrorCode GROUP_MESSAGE_PIN_MAX_LIMIT = new ErrorCode(1_040_400_006, "群置顶消息数量不能超过 {} 条");
    ErrorCode GROUP_MESSAGE_ALREADY_PINNED = new ErrorCode(1_040_400_007, "该消息已置顶");
    ErrorCode GROUP_MESSAGE_NOT_PINNED = new ErrorCode(1_040_400_008, "该消息未置顶");

    // ========== 群成员 (1-040-500-000) ==========
    ErrorCode GROUP_MEMBER_NOT_IN_GROUP = new ErrorCode(1_040_500_001, "您已不在该群中");
    ErrorCode GROUP_OWNER_CANNOT_QUIT = new ErrorCode(1_040_500_003, "群主不能退出群聊，请先转让群主或解散群聊");
    ErrorCode GROUP_CANNOT_REMOVE_SELF = new ErrorCode(1_040_500_004, "不能将自己移出群聊");
    ErrorCode GROUP_MEMBER_EXCEED = new ErrorCode(1_040_500_005, "群聊人数不能超过 {} 人");
    ErrorCode GROUP_INVITE_NOT_FRIEND = new ErrorCode(1_040_500_006, "'{}' 不是您的好友，邀请失败");
    ErrorCode GROUP_ADMIN_TARGET_NOT_IN_GROUP = new ErrorCode(1_040_500_007, "目标用户已不在该群中");
    ErrorCode GROUP_ADMIN_TARGET_IS_OWNER = new ErrorCode(1_040_500_008, "群主无法被设为或撤销管理员");
    ErrorCode GROUP_ADMIN_MAX_LIMIT = new ErrorCode(1_040_500_009, "群管理员数量不能超过 {} 人");
    ErrorCode GROUP_REMOVE_OWNER_DENIED = new ErrorCode(1_040_500_010, "群主无法被移出群聊");
    ErrorCode GROUP_REMOVE_ADMIN_DENIED = new ErrorCode(1_040_500_011, "管理员无法移出其他管理员，请先由群主撤销其管理员身份");
    ErrorCode GROUP_MUTED_CANNOT_SEND = new ErrorCode(1_040_500_012, "群已全局禁言，仅群主和管理员可发送消息");
    ErrorCode GROUP_MEMBER_MUTED_CANNOT_SEND = new ErrorCode(1_040_500_013, "您已被禁言，解除时间：{}");
    ErrorCode GROUP_MUTE_MEMBER_SELF = new ErrorCode(1_040_500_014, "不能禁言自己");
    ErrorCode GROUP_MUTE_OWNER_DENIED = new ErrorCode(1_040_500_015, "群主无法被禁言");
    ErrorCode GROUP_MUTE_ADMIN_DENIED = new ErrorCode(1_040_500_016, "管理员无法禁言其他管理员");

    // ========== 好友 (1-040-600-000) ==========
    ErrorCode FRIEND_NOT_FRIEND = new ErrorCode(1_040_600_001, "对方不是您的好友");
    ErrorCode FRIEND_ADD_SELF = new ErrorCode(1_040_600_002, "不允许添加自己为好友");
    ErrorCode FRIEND_NOT_BLOCKED = new ErrorCode(1_040_600_003, "对方未在黑名单中");
    ErrorCode FRIEND_BLOCKED_BY_PEER = new ErrorCode(1_040_600_004, "您已被对方拉入黑名单，无法发送消息");

    // ========== 加群申请 (1-040-510-000) ==========
    ErrorCode GROUP_REQUEST_NOT_EXISTS = new ErrorCode(1_040_510_001, "加群申请不存在");
    ErrorCode GROUP_REQUEST_HANDLED = new ErrorCode(1_040_510_002, "加群申请已处理");
    ErrorCode GROUP_REQUEST_NOT_TO_ME = new ErrorCode(1_040_510_003, "仅群主或管理员可处理加群申请");
    ErrorCode GROUP_REQUEST_ALREADY_MEMBER = new ErrorCode(1_040_510_004, "您已在该群中，无需重复申请");

    // ========== 好友申请 (1-040-610-000) ==========
    ErrorCode FRIEND_REQUEST_NOT_EXISTS = new ErrorCode(1_040_610_001, "好友申请不存在");
    ErrorCode FRIEND_REQUEST_HANDLED = new ErrorCode(1_040_610_002, "好友申请已处理");
    ErrorCode FRIEND_REQUEST_NOT_TO_ME = new ErrorCode(1_040_610_003, "不能处理别人的好友申请");
    ErrorCode FRIEND_REQUEST_ALREADY_FRIEND = new ErrorCode(1_040_610_005, "您已是 TA 的好友，无需重复添加");
    ErrorCode FRIEND_REQUEST_BLOCKED_BY_PEER = new ErrorCode(1_040_610_006, "您已被对方拉入黑名单，无法添加为好友");

    // ========== 敏感词 (1-040-700-000) ==========
    ErrorCode SENSITIVE_WORD_NOT_EXISTS = new ErrorCode(1_040_700_000, "敏感词不存在");
    ErrorCode SENSITIVE_WORD_DUPLICATED = new ErrorCode(1_040_700_001, "敏感词 '{}' 已存在");

    // ========== 表情包 (1-040-800-000) ==========
    ErrorCode FACE_PACK_NOT_EXISTS = new ErrorCode(1_040_800_000, "表情包不存在");
    ErrorCode FACE_PACK_HAS_ITEMS = new ErrorCode(1_040_800_001, "表情包下还有表情，无法删除");
    ErrorCode FACE_PACK_ITEM_NOT_EXISTS = new ErrorCode(1_040_800_002, "表情不存在");
    ErrorCode FACE_USER_ITEM_NOT_EXISTS = new ErrorCode(1_040_800_010, "个人表情不存在");
    ErrorCode FACE_USER_ITEM_NOT_OWN = new ErrorCode(1_040_800_011, "不能操作他人的表情");
    ErrorCode FACE_USER_ITEM_DUPLICATED = new ErrorCode(1_040_800_013, "该表情已添加到个人表情");

}
