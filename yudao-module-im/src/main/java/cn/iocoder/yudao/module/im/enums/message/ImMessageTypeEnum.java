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
    TIP_TEXT(21, "系统提示", true, false), // 对应 TextMessage 类

    // ========== 好友通知（1201-1210 直接复用 OpenIM 段位编号） ==========
    /**
     * 对应 OpenIM：FriendApplicationApprovedNotification 1201
     * 对应自己的类：FriendRequestApprovedNotification
     * 场景：B 同意 A 的好友申请，推给 A 多端
     */
    FRIEND_REQUEST_APPROVED(1201, "好友申请被同意", false, false),
    /**
     * 对应 OpenIM：FriendApplicationRejectedNotification 1202
     * 对应自己的类：FriendRequestRejectedNotification
     * 场景：B 拒绝 A 的好友申请，推给 A 多端
     */
    FRIEND_REQUEST_REJECTED(1202, "好友申请被拒绝", false, false),
    /**
     * 对应 OpenIM：FriendApplicationNotification 1203
     * 对应自己的类：FriendRequestNotification
     * 场景：A 申请加 B，推给 B 多端，前端落到「新的朋友」列表
     */
    FRIEND_APPLICATION(1203, "收到新的好友申请", false, false),
    /**
     * 对应 OpenIM：FriendAddedNotification 1204
     * 对应自己的类：FriendAddNotification
     * 场景：双方建立好友关系（同意申请 / 管理员导入），推给 A、B 双方多端
     */
    FRIEND_ADD(1204, "新增好友", false, false),
    /**
     * 对应 OpenIM：FriendDeletedNotification 1205
     * 对应自己的类：FriendDeleteNotification
     * 场景：A 删除 B，推给 A、B 双方多端
     */
    FRIEND_DELETE(1205, "好友被删除", false, false),
    // 1206 对应 OpenIM FriendRemarkSetNotification；本系统并入 FRIEND_UPDATE(1210) 统一推送，单一字段变更不再独立通道
    /**
     * 对应 OpenIM：BlackAddedNotification 1207
     * 对应自己的类：FriendBlockNotification
     * 场景：A 拉黑 B，仅推 A 多端
     */
    FRIEND_BLOCK(1207, "加入黑名单", false, false),
    /**
     * 对应 OpenIM：BlackDeletedNotification 1208
     * 对应自己的类：FriendUnblockNotification
     * 场景：A 移出 B 的黑名单，仅推 A 多端
     */
    FRIEND_UNBLOCK(1208, "移出黑名单", false, false),
    /**
     * 对应 OpenIM：FriendInfoUpdatedNotification 1209
     * 对应自己的类：FriendInfoUpdatedNotification
     * 场景：B 改了昵称 / 头像后，推给 B 的所有好友
     * TODO @AI：未实现；待 system 模块改昵称 / 头像时回调 IM 模块批量推此事件，前端 dispatcher 已就绪
     */
    FRIEND_INFO_UPDATED(1209, "好友资料变更", false, false),
    /**
     * 对应 OpenIM：FriendsInfoUpdateNotification 1210（窄化到 muted / pinned 单边属性）
     * 对应自己的类：FriendUpdateNotification
     * 场景：A 改了 muted / pinned 等单边属性，推 A 多端
     */
    FRIEND_UPDATE(1210, "好友信息批量更新", false, false),

    // ========== 群事件（1501-1520 直接复用 OpenIM 段位编号；1530+ 我们独有扩展） ==========
    // 1500 对应 OpenIM GroupNotificationBegin 起始位，仅作占位，不使用
    /**
     * 对应 OpenIM：sdkws.GroupCreatedTips（GroupCreatedNotification 1501）
     * 对应自己的类：GroupCreateNotification
     * 场景：用户创建群（同时邀请初始成员），全员广播（含创建者多端同步 + 初始成员）
     */
    GROUP_CREATE(1501, "群创建", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupInfoSetTips（GroupInfoSetNotification 1502，NAME / NOTICE 之外字段的 generic 兜底）
     * 对应自己的类：GroupInfoUpdateNotification
     * 场景：群主修改群头像 / 简介等字段后全员广播
     */
    GROUP_INFO_UPDATE(1502, "群信息变更", true, false),
    /**
     * 对应 OpenIM：sdkws.JoinGroupApplicationTips（JoinGroupApplicationNotification 1503）
     * TODO @AI 未实现：入群申请；本期不在范围，预留段位以便未来对齐 OpenIM
     */
    GROUP_JOIN_APPLICATION(1503, "入群申请", true, false),
    /**
     * 对应 OpenIM：sdkws.MemberQuitTips（MemberQuitNotification 1504）
     * 对应自己的类：GroupMemberQuitNotification
     * 场景：成员主动退群（send-before-remove），全员广播（含 quitter）；quitter 自判 operatorUserId === self → removeGroup
     */
    GROUP_MEMBER_QUIT(1504, "成员退群", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupApplicationAcceptedTips（GroupApplicationAcceptedNotification 1505）
     * TODO @AI 未实现：入群申请通过；本期不在范围
     */
    GROUP_APPLICATION_ACCEPTED(1505, "入群申请通过", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupApplicationRejectedTips（GroupApplicationRejectedNotification 1506）
     * TODO @AI 未实现：入群申请拒绝；本期不在范围
     */
    GROUP_APPLICATION_REJECTED(1506, "入群申请拒绝", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupOwnerTransferredTips（GroupOwnerTransferredNotification 1507）
     * 对应自己的类：GroupOwnerTransferNotification
     * 场景：群主转让，全员广播；前端 transferOwner 把 ownerUserId 切到新值 + 旧群主 role → NORMAL / 新群主 role → OWNER
     */
    GROUP_OWNER_TRANSFER(1507, "群主转让", true, false),
    /**
     * 对应 OpenIM：sdkws.MemberKickedTips（MemberKickedNotification 1508）
     * 对应自己的类：GroupMemberKickNotification
     * 场景：群主 / 管理员移出成员（send-before-remove），全员广播（含被踢者）；被踢者自判 memberUserIds 含 self → removeGroup
     */
    GROUP_MEMBER_KICK(1508, "成员被移出", true, false),
    /**
     * 对应 OpenIM：sdkws.MemberInvitedTips（MemberInvitedNotification 1509）
     * 对应自己的类：GroupMemberInviteNotification
     * 场景：成员邀请新人入群，全员广播（含被邀请者）；被邀请人前端按 memberUserIds 含自己自判 fetchGroupInfo + fetchGroupMembers bootstrap
     */
    GROUP_MEMBER_INVITE(1509, "成员加入", true, false),
    /**
     * 对应 OpenIM：sdkws.MemberEnterTips（MemberEnterNotification 1510）
     * TODO @AI 未实现：自由进群（链接 / 二维码进群）；本期不在范围
     */
    GROUP_MEMBER_ENTER(1510, "自由进群", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupDismissedTips（GroupDismissedNotification 1511）
     * 对应自己的类：GroupDissolveNotification
     * 场景：群主解散群（send-before-remove），全员广播（含群主多端同步）；前端 removeGroup 清群；离场用户离线 pull 通过 quit 路径（send_time < quit_time）也能拉到
     */
    GROUP_DISSOLVE(1511, "群解散", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupMemberMutedTips（GroupMemberMutedNotification 1512）
     * TODO @AI 未实现：单成员禁言（管理员禁言某成员）；本期发版要实现
     */
    GROUP_MEMBER_MUTED(1512, "成员禁言", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupMemberCancelMutedTips（GroupMemberCancelMutedNotification 1513）
     * TODO @AI 未实现：单成员取消禁言；本期发版要实现
     */
    GROUP_MEMBER_CANCEL_MUTED(1513, "成员取消禁言", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupMutedTips（GroupMutedNotification 1514）
     * TODO @AI 未实现：全群禁言（管理员把整个群设为禁言状态，所有成员都不能发消息）；本期发版要实现
     */
    GROUP_MUTED(1514, "全群禁言", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupCancelMutedTips（GroupCancelMutedNotification 1515）
     * TODO @AI 未实现：全群取消禁言；本期发版要实现
     */
    GROUP_CANCEL_MUTED(1515, "全群取消禁言", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupMemberInfoSetTips（GroupMemberInfoSetNotification 1516，窄化到 displayUserName）
     * 对应自己的类：GroupMemberNicknameUpdateNotification
     * 场景：成员修改自己在群里的昵称，全员广播；前端按 displayUserName 局部更新对应 member
     */
    GROUP_MEMBER_NICKNAME_UPDATE(1516, "成员昵称变更", true, false),
    /**
     * 对应 OpenIM：GroupMemberSetToAdminNotification 1517
     * 对应自己的类：GroupAdminAddNotification
     * 场景：群主设置管理员，全员广播；前端 updateMembersRole 把对应成员 role 提升为 ADMIN
     */
    GROUP_ADMIN_ADD(1517, "添加管理员", true, false),
    /**
     * 对应 OpenIM：GroupMemberSetToOrdinaryUserNotification 1518
     * 对应自己的类：GroupAdminRemoveNotification
     * 场景：群主撤销管理员，全员广播；前端 updateMembersRole 把对应成员 role 降级为 NORMAL
     */
    GROUP_ADMIN_REMOVE(1518, "撤销管理员", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupInfoSetAnnouncementTips（GroupInfoSetAnnouncementNotification 1519）
     * 对应自己的类：GroupNoticeUpdateNotification
     * 场景：群主修改群公告后全员广播
     */
    GROUP_NOTICE_UPDATE(1519, "群公告变更", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupInfoSetNameTips（GroupInfoSetNameNotification 1520）
     * 对应自己的类：GroupNameUpdateNotification
     * 场景：群主修改群名后全员广播
     */
    GROUP_NAME_UPDATE(1520, "群名变更", true, false),

    // 1530+ 我们独有扩展段（OpenIM 1500-1520 段位无对应物）
    /**
     * 对应 OpenIM：无直接对应（OpenIM 走 ConversationChangeNotification 1300 单聊路径）
     * 对应自己的类：GroupMemberSettingUpdateNotification
     * 场景：用户改自己的群免打扰 / 群备注，仅推该用户其他在线终端做多端同步
     */
    GROUP_MEMBER_SETTING_UPDATE(1530, "群成员个人设置变更", false, false),
    /**
     * 对应 OpenIM：无（OpenIM 无群消息置顶功能，自有扩展）
     * 对应自己的类：GroupMessagePinNotification
     * 场景：群主 / 管理员置顶一条群消息，全员广播；payload 直接带消息对象，前端把 message push 进 group.pinnedMessages
     */
    GROUP_MESSAGE_PIN(1531, "群消息置顶", true, false),
    /**
     * 对应 OpenIM：无（OpenIM 无群消息置顶功能，自有扩展）
     * 对应自己的类：GroupMessageUnpinNotification
     * 场景：群主 / 管理员取消置顶，全员广播；前端按 messageId 从 group.pinnedMessages 移除
     */
    GROUP_MESSAGE_UNPIN(1532, "群消息取消置顶", true, false);

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
