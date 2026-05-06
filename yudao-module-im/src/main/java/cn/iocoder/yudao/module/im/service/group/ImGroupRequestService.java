package cn.iocoder.yudao.module.im.service.group;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.request.ImGroupRequestApplyReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupRequestManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupRequestDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * IM 加群申请 Service 接口
 *
 * @author 芋道源码
 */
public interface ImGroupRequestService {

    /**
     * 用户主动申请加群
     * <p>
     * 群 joinType=FREE 时直接入群 + 1510 全员广播 + 1505 推送给申请人；
     * 否则落 im_group_request 待审批 + 1503 定向推送给群主 / 全部管理员
     *
     * @param userId 申请人用户编号
     * @param reqVO  申请请求
     * @return 申请记录；自由进群直进时返回 null
     */
    ImGroupRequestDO applyJoinGroup(Long userId, @Valid ImGroupRequestApplyReqVO reqVO);

    /**
     * 同意加群申请（群主或管理员）
     * <p>
     * 处理前校验入群人数上限；通过后写 group_member（带 addSource / inviterUserId） + 推送通知
     *
     * @param userId    操作人用户编号
     * @param requestId 申请记录编号
     */
    void agreeGroupRequest(Long userId, Long requestId);

    /**
     * 拒绝加群申请（群主或管理员）
     *
     * @param userId        操作人用户编号
     * @param requestId     申请记录编号
     * @param handleContent 拒绝理由
     */
    void refuseGroupRequest(Long userId, Long requestId, String handleContent);

    /**
     * 邀请创建审批申请（用于 inviteGroupMember 在 joinType=APPLY_AND_NORMAL_INVITE 且邀请人是普通成员时调用）
     * <p>
     * 每个被邀请人创建一条 inviter_user_id=操作人 的待审批记录；通知群主 / 全部管理员
     *
     * @param groupId         群编号
     * @param inviterUserId   邀请人用户编号
     * @param invitedUserIds  被邀请人用户编号集合
     */
    void createInviteRequests(Long groupId, Long inviterUserId, Collection<Long> invitedUserIds);

    /**
     * 拉取「我相关」加群申请列表（含我主动申请、我被邀请待审）；游标分页
     *
     * @param userId        用户编号
     * @param lastRequestId 当前列表最旧记录的 id；首页传 null
     * @param limit         单次拉取条数
     * @return 申请记录列表，按 id 倒序
     */
    List<ImGroupRequestDO> getMyGroupRequestList(Long userId, Long lastRequestId, Integer limit);

    /**
     * 拉取指定群下未处理申请；仅群主 / 管理员可调
     *
     * @param userId        操作人用户编号
     * @param groupId       群编号
     * @param lastRequestId 游标 id
     * @param limit         单次拉取条数
     * @return 未处理申请列表
     */
    List<ImGroupRequestDO> getPendingGroupRequestList(Long userId, Long groupId, Long lastRequestId, Integer limit);

    /**
     * 按 id 单查申请记录；通用读接口，越权过滤交由调用方
     *
     * @param id 申请记录编号
     * @return 申请记录
     */
    ImGroupRequestDO getGroupRequest(Long id);

    /**
     * 批量统计指定群的未处理申请数量；用于 ImGroupRespVO.pendingRequestCount 回填
     *
     * @param groupIds 群编号集合
     * @return 群编号 → 未处理申请数 Map
     */
    Map<Long, Long> getPendingCountMap(Collection<Long> groupIds);

    // ==================== 管理后台 ====================

    /**
     * 【管理后台】分页查询加群申请记录
     */
    PageResult<ImGroupRequestDO> getGroupRequestPage(ImGroupRequestManagerPageReqVO reqVO);

}
