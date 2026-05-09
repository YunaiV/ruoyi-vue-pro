package cn.iocoder.yudao.module.im.service.rtc.bo;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

// TODO @AI：后续讨论；抽成 DO；写到 mysql 里；
/**
 * IM 通话会话 BO；存放在 {@link ImCallSessionStore} 内存里，通话结束即移除
 *
 * @author 芋道源码
 */
@Data
@Builder
public class ImCallSessionBO {

    /**
     * 业务通话编号；雪花 ID 生成；用于消息流落库与 Webhook 关联
     */
    private String callId;
    /**
     * LiveKit 房间名；私聊由好友对派生 call_friend_{minId}_{maxId}；群聊为 call_group_{groupId}
     */
    private String roomName;
    /**
     * 会话场景；取自 {@link cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum} 的 type
     */
    private Integer scene;
    /**
     * 媒体类型；取自 {@link cn.iocoder.yudao.module.im.enums.rtc.ImCallMediaTypeEnum} 的 type
     */
    private Integer mediaType;
    /**
     * 发起人编号
     */
    private Long inviterId;
    /**
     * 被邀请人集合；私聊为单元素；群聊为发起时所有候选成员（含发起人本人，方便统一遍历）
     */
    private Set<Long> inviteeIds;
    /**
     * 群编号；群聊场景必填
     */
    private Long groupId;
    /**
     * 状态；取自 {@link cn.iocoder.yudao.module.im.enums.rtc.ImCallStatusEnum} 的 type
     */
    private Integer status;
    /**
     * 通话开始时间戳；millis
     */
    private Long startTime;
    /**
     * 已加入房间的成员集合
     */
    @Builder.Default
    private Set<Long> joinedUserIds = new LinkedHashSet<>();
    /**
     * 已拒绝的成员集合（接通前点拒接）
     */
    @Builder.Default
    private Set<Long> rejectedUserIds = new LinkedHashSet<>();
    /**
     * 已离开的成员集合（接通后挂断）
     */
    @Builder.Default
    private Set<Long> leftUserIds = new LinkedHashSet<>();

    // TODO @AI：bo 里，不要有这种方法；
    /**
     * 当前会话中所有相关用户；用于 RTC_END 信号扇出与忙线清理
     */
    public Set<Long> getAllUserIds() {
        Set<Long> result = new LinkedHashSet<>();
        result.add(inviterId);
        if (inviteeIds != null) {
            result.addAll(inviteeIds);
        }
        result.addAll(joinedUserIds);
        return result;
    }

    // TODO @AI：这个方法，是不是没在使用？？？
    /**
     * 判断用户是否参与该通话；不在被邀请也不在已加入即非参与方
     */
    public boolean containsUser(Long userId) {
        if (userId == null) {
            return false;
        }
        if (userId.equals(inviterId)) {
            return true;
        }
        if (inviteeIds != null && inviteeIds.contains(userId)) {
            return true;
        }
        return joinedUserIds.contains(userId);
    }

}
