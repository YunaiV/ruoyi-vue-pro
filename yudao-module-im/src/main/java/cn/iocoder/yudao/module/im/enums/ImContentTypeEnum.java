package cn.iocoder.yudao.module.im.enums;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;

/**
 * IM 内容类型枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum ImContentTypeEnum implements ArrayValuable<Integer> {

    // ========== 用户聊天消息（101-105 直接复用 OpenIM 段位编号） ==========
    /**
     * 对应 OpenIM：Text 101
     * 对应自己的类：TextMessage
     */
    TEXT(101, "文本", true, true),
    /**
     * 对应 OpenIM：Picture 102
     * 对应自己的类：ImageMessage
     */
    IMAGE(102, "图片", true, true),
    /**
     * 对应 OpenIM：Sound 103
     * 对应自己的类：AudioMessage
     */
    VOICE(103, "语音", true, true),
    /**
     * 对应 OpenIM：Video 104
     * 对应自己的类：VideoMessage
     */
    VIDEO(104, "视频", true, true),
    /**
     * 对应 OpenIM：File 105
     * 对应自己的类：FileMessage
     */
    FILE(105, "文件", true, true),
    /**
     * 对应 OpenIM：Merger 107
     * 对应自己的类：MergeMessage
     */
    MERGE(107, "合并转发", true, true),
    /**
     * 对应 OpenIM：Card 108（OpenIM 仅用户名片；本系统扩展为用户 / 群双类型，按 targetType 区分）
     * 对应自己的类：CardMessage
     * 场景：把用户名片 / 群名片推荐给其他会话；用户名片点击打开 UserInfoCard，群名片点击「已加群跳会话 / 未加群弹申请加群」
     */
    CARD(108, "名片", true, true),
    /**
     * 对应 OpenIM：Face 115
     * 对应自己的类：FaceMessage
     * 场景：表情贴图（运营配置的系统表情包 + 用户私有表情包）；Unicode emoji 仍走 TEXT
     */
    FACE(115, "表情", true, true),

    // ========== 频道消息扩展段（125+；OpenIM 122 之后未占用，本系统在 125 起步给频道 / 公众号类消息扩展） ==========
    /**
     * 对应 OpenIM：无（125 段位 OpenIM 未占用，作为频道消息扩展起始位）
     * 对应自己的类：MaterialMessage
     * 场景：频道运营推送的素材消息；当前形态为图文卡片（title + coverUrl + summary + url）
     * 详情：url 非空跳 url；url 为空时客户端按 materialId 拉 /get-content 渲染富文本正文
     */
    MATERIAL(125, "素材", true, true),

    // ========== 信号类（2101 / 2200 直接复用 OpenIM 段位编号；2201 自有扩展） ==========
    /**
     * 对应 OpenIM：RevokeNotification 2101
     * 对应自己的类：RecallMessage
     */
    RECALL(2101, "撤回", true, false),
    /**
     * 对应 OpenIM：HasReadReceipt 2200
     * 对应自己的类：无（payload 走 ImXxxMessageDTO 顶层字段）
     */
    RECEIPT(2200, "回执", false, false),
    /**
     * 对应 OpenIM：无（自有扩展，OpenIM 走 ConversationChangeNotification 1300 路径）
     * 对应自己的类：无（payload 走 ImXxxMessageDTO 顶层字段）
     */
    READ(2201, "已读", false, false),

    // ========== 实时通话信令（1601-1605 段位与 OpenIM 对齐；1610+ 自有扩展） ==========
    /**
     * 对应 OpenIM：SignalingNotification 1601（通话信令统一入口）
     * 对应自己的类：ImRtcCallNotification
     * 场景：通话信令；不入库，走 imWebSocketService 仅推参与方；status 复用参与者状态枚举区分 INVITING / JOINED / REJECTED / NO_ANSWER / LEFT
     */
    RTC_CALL(1601, "通话信令", false, false),
    /**
     * 对应 OpenIM：RoomParticipantsConnectedNotification 1602
     * 对应自己的类：ImRtcParticipantConnectedNotification
     * 场景：通话参与者加入；LiveKit webhook participant_joined 触发；私聊推 peer 多端 + inviter 多端，群聊全群广播；不入库
     */
    RTC_PARTICIPANT_CONNECTED(1602, "通话参与者加入", false, false),
    /**
     * 对应 OpenIM：RoomParticipantsDisconnectedNotification 1603
     * 对应自己的类：ImRtcParticipantDisconnectedNotification
     * 场景：通话参与者离开；LiveKit webhook participant_left 触发；推送范围同 1602；不入库
     */
    RTC_PARTICIPANT_DISCONNECTED(1603, "通话参与者离开", false, false),
    // 1604-1609 OpenIM 已用 / 留作扩展，本系统暂不使用
    /**
     * 对应 OpenIM：无（自有扩展，OpenIM 通话事件不入消息流）
     * 对应自己的类：ImRtcCallStartNotification
     * 场景：通话开始；群聊入 im_group_message 全群广播，前端渲染聊天 tip「{inviterNickname} 发起了语音通话」；
     * 私聊入 im_private_message 定向给被叫，仅用于会话列表预览展示「[语音通话]」（不渲染聊天 tip）
     * <p>
     * 与 RTC_CALL_END(1611) 两段式配对：START 一定先于 END 入库（START 在 invite 接口事务里、END 在 cancel/leave 接口事务里，自然按请求顺序串行）
     */
    RTC_CALL_START(1610, "通话开始", true, false),
    /**
     * 对应 OpenIM：无（自有扩展，OpenIM 通话事件不入消息流）
     * 对应自己的类：ImRtcCallEndNotification
     * 场景：通话结束；入 im_private_message / im_group_message；私聊渲染准气泡，群聊渲染 tip「语音通话已经结束」
     * <p>
     * 与 RTC_CALL_START(1610) 两段式配对
     */
    RTC_CALL_END(1611, "通话结束", true, false),

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
    FRIEND_REQUEST_RECEIVED(1203, "收到新的好友申请", false, false),
    /**
     * 对应 OpenIM：FriendAddedNotification 1204（OpenIM friendAdded.isSendMsg=false 默认不入消息流；本系统改为入库当会话气泡）
     * 对应自己的类：FriendAddNotification
     * 场景：双方建立好友关系，单条入库（sender=fromUserId, receiver=toUserId）；双向 WebSocket 自动覆盖双方多端
     * 注意：silentReAddFriend 单边语义场景，发送时显式 setPersistent(false) 覆盖默认值
     */
    FRIEND_ADD(1204, "新增好友", true, false),
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
     * 触发：system 模块发 AdminUserProfileUpdateMessage，IM 消费者 AdminUserProfileUpdateConsumer 批量推此通知
     */
    FRIEND_INFO_UPDATED(1209, "好友资料变更", false, false),
    /**
     * 对应 OpenIM：FriendsInfoUpdateNotification 1210（窄化到 silent / pinned 单边属性）
     * 对应自己的类：FriendUpdateNotification
     * 场景：A 改了 silent / pinned 等单边属性，推 A 多端
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
     * 对应自己的类：GroupRequestReceivedNotification
     * 场景：用户申请加群 / 普通成员邀请待审批，定向私聊推送给群主 + 全部管理员（多端同步）；不入群消息流
     */
    GROUP_REQUEST_RECEIVED(1503, "收到新的入群申请", false, false),
    /**
     * 对应 OpenIM：sdkws.MemberQuitTips（MemberQuitNotification 1504）
     * 对应自己的类：GroupMemberQuitNotification
     * 场景：成员主动退群（send-before-remove），全员广播（含 quitter）；quitter 自判 operatorUserId === self → removeGroup
     */
    GROUP_MEMBER_QUIT(1504, "成员退群", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupApplicationAcceptedTips（GroupApplicationAcceptedNotification 1505）
     * 对应自己的类：GroupRequestApprovedNotification
     * 场景：群主 / 管理员同意申请，定向私聊推送给申请人 + 群主 + 全部管理员；申请人侧弹 toast，admin 侧 pendingRequestCount-1；不入群消息流
     */
    GROUP_REQUEST_APPROVED(1505, "入群申请被同意", false, false),
    /**
     * 对应 OpenIM：sdkws.GroupApplicationRejectedTips（GroupApplicationRejectedNotification 1506）
     * 对应自己的类：GroupRequestRejectedNotification
     * 场景：群主 / 管理员拒绝申请，定向私聊推送给申请人 + 群主 + 全部管理员；不入群消息流
     */
    GROUP_REQUEST_REJECTED(1506, "入群申请被拒绝", false, false),
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
     * 场景：成员邀请新人入群，全员广播（含被邀请者）；被邀请人前端按 memberUserIds 含自己自判，初次拉取 fetchGroupInfo + fetchGroupMembers
     */
    GROUP_MEMBER_INVITE(1509, "成员加入", true, false),
    /**
     * 对应 OpenIM：sdkws.MemberEnterTips（MemberEnterNotification 1510）
     * 对应自己的类：GroupMemberEnterNotification
     * 场景：用户经搜索 / 二维码 / 分享链接自由进群（FREE 模式或审批通过后），全员广播；前端按 entrantUserId 局部添加成员
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
     * 对应自己的类：GroupMemberMutedNotification
     * 场景：群主 / 管理员禁言某成员，全员广播
     */
    GROUP_MEMBER_MUTED(1512, "成员禁言", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupMemberCancelMutedTips（GroupMemberCancelMutedNotification 1513）
     * 对应自己的类：GroupMemberCancelMutedNotification
     * 场景：群主 / 管理员取消某成员禁言，全员广播
     */
    GROUP_MEMBER_CANCEL_MUTED(1513, "成员取消禁言", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupMutedTips（GroupMutedNotification 1514）
     * 对应自己的类：GroupMutedNotification
     * 场景：群主 / 管理员开启全群禁言，全员广播
     */
    GROUP_MUTED(1514, "全群禁言", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupCancelMutedTips（GroupCancelMutedNotification 1515）
     * 对应自己的类：GroupCancelMutedNotification
     * 场景：群主 / 管理员取消全群禁言，全员广播
     */
    GROUP_CANCEL_MUTED(1515, "全群取消禁言", true, false),
    /**
     * 对应 OpenIM：sdkws.GroupMemberInfoSetTips（GroupMemberInfoSetNotification 1516，窄化到 displayUserName）
     * 对应自己的类：GroupMemberNicknameUpdateNotification
     * 场景：成员修改自己在群里的昵称，在线成员同步对应 member
     */
    GROUP_MEMBER_NICKNAME_UPDATE(1516, "成员昵称变更", false, false),
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
    GROUP_MESSAGE_UNPIN(1532, "群消息取消置顶", true, false),
    /**
     * 对应 OpenIM：无（OpenIM 无群封禁概念，自有扩展）
     * 对应自己的类：GroupBannedNotification
     * 场景：管理后台封禁 / 解封群，全员广播；前端按 banned 字段切换输入栏封禁覆盖层
     */
    GROUP_BANNED(1533, "群封禁变更", true, false);

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImContentTypeEnum::getType).toArray(Integer[]::new);

    private static final Set<Integer> FRIEND_NOTIFICATION_TYPES = CollUtil.newHashSet(
            FRIEND_REQUEST_APPROVED.type,
            FRIEND_REQUEST_REJECTED.type,
            FRIEND_REQUEST_RECEIVED.type,
            FRIEND_ADD.type,
            FRIEND_DELETE.type,
            FRIEND_BLOCK.type,
            FRIEND_UNBLOCK.type,
            FRIEND_INFO_UPDATED.type,
            FRIEND_UPDATE.type);

    private static final Set<Integer> GROUP_REQUEST_NOTIFICATION_TYPES = CollUtil.newHashSet(
            GROUP_REQUEST_RECEIVED.type,
            GROUP_REQUEST_APPROVED.type,
            GROUP_REQUEST_REJECTED.type);

    private static final Set<Integer> RTC_NOTIFICATION_TYPES = CollUtil.newHashSet(
            RTC_CALL.type,
            RTC_PARTICIPANT_CONNECTED.type,
            RTC_PARTICIPANT_DISCONNECTED.type);

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
    public static ImContentTypeEnum validate(Integer type) {
        ImContentTypeEnum result = ArrayUtil.firstMatch(item -> item.type.equals(type), values());
        Assert.notNull(result, "未注册的消息类型 type={}", type);
        return result;
    }

    /**
     * 判断是否为好友通知
     */
    public static boolean isFriendNotification(Integer type) {
        return type != null && FRIEND_NOTIFICATION_TYPES.contains(type);
    }

    /**
     * 判断是否为群申请定向通知
     */
    public static boolean isGroupRequestNotification(Integer type) {
        return type != null && GROUP_REQUEST_NOTIFICATION_TYPES.contains(type);
    }

    /**
     * 判断是否为通话信令通知
     */
    public static boolean isRtcNotification(Integer type) {
        return type != null && RTC_NOTIFICATION_TYPES.contains(type);
    }

}
