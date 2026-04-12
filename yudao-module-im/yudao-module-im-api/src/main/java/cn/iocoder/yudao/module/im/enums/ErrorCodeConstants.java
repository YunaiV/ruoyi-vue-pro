package cn.iocoder.yudao.module.im.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * IM 错误码枚举类
 * <p>
 * im 系统，使用 1-040-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 会话 （1-040-100-000）  ==========
    ErrorCode CONVERSATION_NOT_EXISTS = new ErrorCode(1_040_100_000, "会话不存在");

    // ========== 收件箱 (1-040-200-000) ==========
    ErrorCode INBOX_NOT_EXISTS = new ErrorCode(1_040_200_000, "收件箱不存在");

    // ========== 消息 (1-040-300-000) ==========
    ErrorCode MESSAGE_NOT_EXISTS = new ErrorCode(1_040_300_000, "消息不存在");
    ErrorCode MESSAGE_RECEIVER_NOT_EXISTS = new ErrorCode(1_040_300_001, "接收人不存在");
    ErrorCode MESSAGE_RECALL_DENIED = new ErrorCode(1_040_300_002, "只能撤回自己发送的消息");
    ErrorCode MESSAGE_ALREADY_RECALLED = new ErrorCode(1_040_300_003, "消息已撤回");
    ErrorCode MESSAGE_SENSITIVE_WORD_BLOCKED = new ErrorCode(1_040_300_004, "消息包含敏感词，无法发送");
    ErrorCode MESSAGE_PULL_SIZE_EXCEEDED = new ErrorCode(1_040_300_005, "单次拉取消息数量超出限制");
    ErrorCode MESSAGE_CLIENT_ID_DUPLICATE = new ErrorCode(1_040_300_006, "消息已发送，请勿重复提交");

    // ========== 群 (1-040-400-000) ==========
    ErrorCode GROUP_NOT_EXISTS = new ErrorCode(1_040_400_000, "群不存在");
    ErrorCode GROUP_BANNED = new ErrorCode(1_040_400_001, "群已被封禁");
    ErrorCode GROUP_DISSOLVED = new ErrorCode(1_040_400_002, "群已解散");

    // ========== 群成员 (1-040-500-000) ==========
    ErrorCode GROUP_MEMBER_NOT_EXISTS = new ErrorCode(1_040_500_000, "群成员不存在");
    ErrorCode GROUP_MEMBER_NOT_IN_GROUP = new ErrorCode(1_040_500_001, "您已不在该群中");
    ErrorCode GROUP_MEMBER_ALREADY_QUIT = new ErrorCode(1_040_500_002, "您已退出该群");

    // ========== 好友 (1-040-600-000) ==========
    ErrorCode FRIEND_NOT_EXISTS = new ErrorCode(1_040_600_000, "好友关系不存在");
    ErrorCode FRIEND_NOT_FRIEND = new ErrorCode(1_040_600_001, "对方不是您的好友");

    // ========== 敏感词 (1-040-700-000) ==========
    ErrorCode SENSITIVE_WORD_NOT_EXISTS = new ErrorCode(1_040_700_000, "敏感词不存在");

}
