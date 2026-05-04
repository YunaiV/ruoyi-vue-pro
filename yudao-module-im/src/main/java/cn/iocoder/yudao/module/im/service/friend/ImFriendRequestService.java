package cn.iocoder.yudao.module.im.service.friend;

import cn.iocoder.yudao.module.im.controller.admin.friend.vo.request.ImFriendRequestApplyReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendRequestDO;

import java.util.List;

/**
 * IM 好友申请 Service 接口
 *
 * @author 芋道源码
 */
public interface ImFriendRequestService {

    /**
     * 发起好友申请
     *
     * @param fromUserId 发起方用户编号
     * @param reqVO      申请请求
     * @return 申请记录
     */
    ImFriendRequestDO applyFriend(Long fromUserId, ImFriendRequestApplyReqVO reqVO);

    /**
     * 同意好友申请
     *
     * @param userId    操作人用户编号
     * @param requestId 申请记录编号
     */
    void agreeFriendRequest(Long userId, Long requestId);

    /**
     * 拒绝好友申请
     *
     * @param userId        操作人用户编号
     * @param requestId     申请记录编号
     * @param handleContent 拒绝理由
     */
    void refuseFriendRequest(Long userId, Long requestId, String handleContent);

    /**
     * 查询「我相关」的申请列表（含我发起的、别人加我的）
     *
     * @param userId 用户编号
     * @return 申请记录列表
     */
    List<ImFriendRequestDO> getMyFriendRequestList(Long userId);

}
