package cn.iocoder.yudao.module.im.service.group;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.request.ImGroupRequestApplyReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupRequestManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupRequestDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * IM 加群申请 Service 接口
 *
 * @author 芋道源码
 */
public interface ImGroupRequestService {

    /**
     * 用户主动申请加群
     * <p>
     * 群未开启审批时直接入群 + 1510 全员广播；开启审批则 upsert 一条 inviterUserId=null 的待审批记录 + 1503 推送
     *
     * @param userId 申请人用户编号
     * @param reqVO  申请请求
     * @return 申请记录；自由进群直进时返回 null
     */
    ImGroupRequestDO applyJoinGroup(Long userId, @Valid ImGroupRequestApplyReqVO reqVO);

    /**
     * 同意加群申请（群主或管理员）；处理前校验入群人数上限
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
     * 邀请创建审批申请；inviteGroupMember 在群开启审批时调用，每个被邀请人 upsert 一条 inviterUserId=操作人 的记录
     *
     * @param groupId         群编号
     * @param inviterUserId   邀请人用户编号
     * @param invitedUserIds  被邀请人用户编号集合
     */
    void createInviteRequestList(Long groupId, Long inviterUserId, Collection<Long> invitedUserIds);

    /**
     * 拉取「我相关」加群申请列表（含我主动申请、我被邀请待审）；游标分页
     *
     * @param userId        用户编号
     * @param lastRequestId 当前列表最旧记录的 id；首页传 null
     * @param limit         单次拉取条数
     * @return 申请记录列表，按 id 倒序
     */
    List<ImGroupRequestDO> getGroupRequestListByUserId(Long userId, Long lastRequestId, Integer limit);

    /**
     * 拉取「我管理的所有群」下的未处理申请列表
     * <p>
     * 前端 store 据此派生：每个群的未处理总数（用于群顶部横幅红点）+ 列表内容（用于 Drawer）
     *
     * @param userId 当前用户编号；后端按 ImGroupMember.role 过滤出我作为 OWNER / ADMIN 的群
     * @return 未处理申请列表（不分页）
     */
    List<ImGroupRequestDO> getUnhandledRequestListByOwnerOrAdmin(Long userId);

    /**
     * 按 id 单查申请记录；通用读接口，越权过滤交由调用方
     *
     * @param id 申请记录编号
     * @return 申请记录
     */
    ImGroupRequestDO getGroupRequest(Long id);

    // ==================== 管理后台 ====================

    /**
     * 【管理后台】分页查询加群申请记录
     */
    PageResult<ImGroupRequestDO> getGroupRequestPage(ImGroupRequestManagerPageReqVO reqVO);

}
