package cn.iocoder.yudao.module.im.service.friend;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.request.ImFriendRequestApplyReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendRequestDO;
import cn.iocoder.yudao.module.im.dal.mysql.friend.ImFriendRequestMapper;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendRequestHandleResultEnum;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendStateEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.friend.FriendRequestApprovedNotification;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.friend.FriendRequestNotification;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.friend.FriendRequestRejectedNotification;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;

/**
 * IM 好友申请 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class ImFriendRequestServiceImpl implements ImFriendRequestService {

    @Resource
    private ImFriendRequestMapper friendRequestMapper;

    @Resource
    private ImFriendService friendService;
    @Resource
    private ImWebSocketService websocketService;

    @Resource
    private ImProperties imProperties;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImFriendRequestDO applyFriend(Long fromUserId, ImFriendRequestApplyReqVO reqVO) {
        Long toUserId = reqVO.getToUserId();
        // 1.1 校验：不能加自己
        if (Objects.equals(fromUserId, toUserId)) {
            throw exception(FRIEND_ADD_SELF);
        }
        // 1.2 校验对方存在且启用
        adminUserApi.validateUser(toUserId);
        // 1.3 已是好友 / 被对方拉黑：直接报错（state 一次拿到双向状态，省两次单边查询）
        ImFriendStateEnum state = friendService.getFriendState(fromUserId, toUserId);
        if (state == ImFriendStateEnum.FRIEND) {
            throw exception(FRIEND_REQUEST_ALREADY_FRIEND);
        }
        if (state == ImFriendStateEnum.BLOCKED) {
            throw exception(FRIEND_REQUEST_BLOCKED_BY_PEER);
        }
        // 1.4 单向好友（我已删 + 对方仍把我当好友）：静默重新启用我侧关系，避免对方感知我曾删除
        // TODO DONE @AI：前端 FriendAddDialog 按 requestId 是否为 null 区分提示文案：null → 「已添加为好友」；非 null → 「等待对方验证」
        ImFriendDO peerFriend = friendService.getFriend(toUserId, fromUserId);
        if (peerFriend != null && CommonStatusEnum.isEnable(peerFriend.getStatus())) {
            friendService.silentReAddFriend(fromUserId, toUserId, reqVO.getDisplayName(), reqVO.getAddSource());
            return null;
        }

        // 2. 落库：复用最新一条未处理记录；否则新建
        ImFriendRequestDO request = friendRequestMapper.selectLatestByFromUserIdAndToUserId(fromUserId, toUserId);
        if (request != null && ImFriendRequestHandleResultEnum.isUnhandled(request.getHandleResult())) {
            // 复用未处理记录：刷新申请理由 / 备注 / 来源；createTime 维持首次申请时间不变（与列表 id DESC 排序一致）
            BeanUtils.copyProperties(reqVO, request);
            friendRequestMapper.updateById(request);
        } else {
            request = BeanUtils.toBean(reqVO, ImFriendRequestDO.class)
                    .setFromUserId(fromUserId).setToUserId(toUserId)
                    .setHandleResult(ImFriendRequestHandleResultEnum.UNHANDLED.getResult());
            friendRequestMapper.insert(request);
        }

        // 3. 推送 FRIEND_APPLICATION 给 toUser 多端
        FriendRequestNotification payload = (FriendRequestNotification) new FriendRequestNotification()
                .setRequestId(request.getId()).setApplyContent(request.getApplyContent()).setAddSource(request.getAddSource())
                .setOperatorUserId(fromUserId).setFriendUserId(fromUserId);
        websocketService.sendPrivateMessageAsync(toUserId, ImPrivateMessageDTO.ofFriendNotification(
                ImMessageTypeEnum.FRIEND_APPLICATION.getType(), fromUserId, toUserId, payload));

        // 4. 全局自动通过开关：注册 afterCommit 回调，事务提交后再走同意流程
        if (imProperties.getFriend().isAutoAccept()) {
            Long requestId = request.getId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    getSelf().agreeFriendRequest(toUserId, requestId);
                }

            });
        }
        return request;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agreeFriendRequest(Long userId, Long requestId) {
        // 1. 校验申请存在 + 未处理 + 操作人是接收方（fail-fast；并发场景仍由下面的乐观锁兜底）
        ImFriendRequestDO request = validateRequestForHandle(userId, requestId);

        // 2. 乐观锁更新申请：handleResult=AGREED + handleTime；并发同意会有一方 affectedRows=0
        ImFriendRequestDO updateObj = new ImFriendRequestDO()
                .setHandleResult(ImFriendRequestHandleResultEnum.AGREED.getResult()).setHandleTime(LocalDateTime.now());
        int affected = friendRequestMapper.updateByIdAndHandleResult(request.getId(),
                ImFriendRequestHandleResultEnum.UNHANDLED.getResult(), updateObj);
        if (affected == 0) {
            throw exception(FRIEND_REQUEST_HANDLED);
        }
        request.setHandleResult(ImFriendRequestHandleResultEnum.AGREED.getResult()).setHandleTime(updateObj.getHandleTime());

        // 3. 双向建立好友关系
        friendService.becomeFriends(request);

        // 4. 推 FRIEND_REQUEST_APPROVED 给 fromUser 多端
        FriendRequestApprovedNotification payload = (FriendRequestApprovedNotification)
                new FriendRequestApprovedNotification().setRequestId(request.getId())
                        .setOperatorUserId(userId).setFriendUserId(userId);
        websocketService.sendPrivateMessageAsync(request.getFromUserId(), ImPrivateMessageDTO.ofFriendNotification(
                ImMessageTypeEnum.FRIEND_REQUEST_APPROVED.getType(), userId, request.getFromUserId(), payload));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuseFriendRequest(Long userId, Long requestId, String handleContent) {
        // 1. 校验申请存在 + 未处理 + 操作人是接收方（fail-fast；并发场景仍由下面的乐观锁兜底）
        ImFriendRequestDO request = validateRequestForHandle(userId, requestId);

        // 2. 乐观锁更新申请：handleResult=REFUSED + handleContent + handleTime；并发拒绝会有一方 affectedRows=0
        ImFriendRequestDO updateObj = new ImFriendRequestDO()
                .setHandleResult(ImFriendRequestHandleResultEnum.REFUSED.getResult())
                .setHandleContent(handleContent).setHandleTime(LocalDateTime.now());
        int affected = friendRequestMapper.updateByIdAndHandleResult(request.getId(),
                ImFriendRequestHandleResultEnum.UNHANDLED.getResult(), updateObj);
        if (affected == 0) {
            throw exception(FRIEND_REQUEST_HANDLED);
        }

        // 3. 推 FRIEND_REQUEST_REJECTED 给 fromUser 多端
        FriendRequestRejectedNotification payload = (FriendRequestRejectedNotification)
                new FriendRequestRejectedNotification().setRequestId(request.getId())
                        .setHandleContent(handleContent)
                        .setOperatorUserId(userId).setFriendUserId(userId);
        websocketService.sendPrivateMessageAsync(request.getFromUserId(), ImPrivateMessageDTO.ofFriendNotification(
                ImMessageTypeEnum.FRIEND_REQUEST_REJECTED.getType(), userId, request.getFromUserId(), payload));
    }

    @Override
    public List<ImFriendRequestDO> getMyFriendRequestList(Long userId) {
        return friendRequestMapper.selectMyList(userId, imProperties.getFriend().getRequestListMaxSize());
    }

    /**
     * 校验申请可被「当前用户」处理：申请存在 + 未处理 + 操作人 = 接收方
     */
    private ImFriendRequestDO validateRequestForHandle(Long userId, Long requestId) {
        ImFriendRequestDO request = friendRequestMapper.selectById(requestId);
        if (request == null) {
            throw exception(FRIEND_REQUEST_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(request.getToUserId(), userId)) {
            throw exception(FRIEND_REQUEST_NOT_TO_ME);
        }
        if (!ImFriendRequestHandleResultEnum.isUnhandled(request.getHandleResult())) {
            throw exception(FRIEND_REQUEST_HANDLED);
        }
        return request;
    }

    private ImFriendRequestServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
