package cn.iocoder.yudao.module.im.service.friend;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.request.ImFriendRequestApplyReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo.ImFriendRequestManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendRequestDO;
import cn.iocoder.yudao.module.im.dal.mysql.friend.ImFriendRequestMapper;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendRequestHandleResultEnum;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendStateEnum;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.notification.friend.FriendRequestApprovedNotification;
import cn.iocoder.yudao.module.im.service.websocket.notification.friend.FriendRequestNotification;
import cn.iocoder.yudao.module.im.service.websocket.notification.friend.FriendRequestRejectedNotification;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
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
        // 错误码与 ImFriendService#validateFriend 不同（语义为「申请被拒」），故保留 inline
        Integer state = friendService.getFriendState(fromUserId, toUserId);
        if (ImFriendStateEnum.isFriend(state)) {
            throw exception(FRIEND_REQUEST_ALREADY_FRIEND);
        }
        if (ImFriendStateEnum.isBlocked(state)) {
            throw exception(FRIEND_REQUEST_BLOCKED_BY_PEER);
        }
        // 1.4 单向好友（我已删 + 对方仍把我当好友）：静默重新启用我侧关系，避免对方感知我曾删除
        ImFriendDO peerFriend = friendService.getFriend(toUserId, fromUserId);
        if (peerFriend != null && CommonStatusEnum.isEnable(peerFriend.getStatus())) {
            // 对方已拉黑：静默恢复等于绕过拉黑回到好友列表，必须先拒掉；
            // getFriendState 在我侧 DISABLE 时直接返回 NONE，拿不到 BLOCKED 信号，这里显式补一次校验
            if (BooleanUtil.isTrue(peerFriend.getBlocked())) {
                throw exception(FRIEND_REQUEST_BLOCKED_BY_PEER);
            }
            friendService.silentReAddFriend(fromUserId, toUserId, reqVO.getDisplayName(), reqVO.getAddSource());
            return null;
        }

        // 2. 落库：同一申请人和接收人唯一，已有记录覆盖申请内容并重置为未处理
        ImFriendRequestDO request = createOrResetRequest(fromUserId, reqVO);

        // 3. 推送 FRIEND_REQUEST_RECEIVED 给 toUser 多端；payload 携带申请方昵称 / 头像，前端按 requestId 直推 push 进列表
        AdminUserRespDTO fromUser = adminUserApi.getUser(fromUserId);
        FriendRequestNotification payload = (FriendRequestNotification) new FriendRequestNotification()
                .setRequestId(request.getId()).setApplyContent(request.getApplyContent()).setAddSource(request.getAddSource())
                .setOperatorUserId(fromUserId).setFriendUserId(fromUserId);
        if (fromUser != null) {
            payload.setFromNickname(fromUser.getNickname()).setFromAvatar(fromUser.getAvatar());
        }
        websocketService.sendNotificationAsync(toUserId, ImConversationTypeEnum.NONE.getType(),
                ImContentTypeEnum.FRIEND_REQUEST_RECEIVED.getType(), payload);

        // 4. 全局自动通过开关：注册 afterCommit 回调，事务提交后再走同意流程
        //    回调内 try/catch 兜底 —— afterCommit 异常会被 Spring 静默吞掉，否则同意失败时申请方永远等不到 APPROVED
        if (imProperties.getFriend().isAutoAccept()) {
            Long requestId = request.getId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    try {
                        getSelf().agreeFriendRequest(toUserId, requestId);
                    } catch (Exception e) {
                        log.error("[applyFriend][autoAccept fromUserId={} toUserId={} requestId={} 自动通过失败]",
                                fromUserId, toUserId, requestId, e);
                    }
                }

            });
        }
        return request;
    }

    /**
     * 创建或重置好友申请
     *
     * @param fromUserId 申请人用户编号
     * @param reqVO      申请请求
     * @return 申请记录
     */
    private ImFriendRequestDO createOrResetRequest(Long fromUserId, ImFriendRequestApplyReqVO reqVO) {
        Long toUserId = reqVO.getToUserId();
        ImFriendRequestDO request = friendRequestMapper.selectByFromUserIdAndToUserId(fromUserId, toUserId);
        if (request == null) {
            // 1. 无旧申请：创建新申请；唯一键冲突时回查并复用并发写入的记录
            request = BeanUtils.toBean(reqVO, ImFriendRequestDO.class)
                    .setFromUserId(fromUserId).setToUserId(toUserId)
                    .setHandleResult(ImFriendRequestHandleResultEnum.UNHANDLED.getResult());
            try {
                friendRequestMapper.insert(request);
                return request;
            } catch (DuplicateKeyException ex) {
                request = friendRequestMapper.selectByFromUserIdAndToUserId(fromUserId, toUserId);
                if (request == null) {
                    throw ex;
                }
            }
        }

        // 2. 复用旧申请：覆盖本次申请内容，并重置为未处理
        LocalDateTime now = LocalDateTime.now();
        friendRequestMapper.updateByIdReset(request.getId(),
                reqVO.getApplyContent(), reqVO.getDisplayName(), reqVO.getAddSource(), now);
        // 同步内存对象，后续通知和自动通过直接复用
        request.setApplyContent(reqVO.getApplyContent()).setDisplayName(reqVO.getDisplayName())
                .setAddSource(reqVO.getAddSource())
                .setHandleResult(ImFriendRequestHandleResultEnum.UNHANDLED.getResult())
                .setHandleContent(null).setHandleTime(null).setUpdateTime(now);
        return request;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agreeFriendRequest(Long userId, Long requestId) {
        // 1.1 校验申请存在、未处理、操作人是接收方
        ImFriendRequestDO request = validateRequestForHandle(userId, requestId);

        // 2. 乐观锁更新申请处理结果
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
        websocketService.sendNotificationAsync(request.getFromUserId(), ImConversationTypeEnum.NONE.getType(),
                ImContentTypeEnum.FRIEND_REQUEST_APPROVED.getType(), payload);
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
        websocketService.sendNotificationAsync(request.getFromUserId(), ImConversationTypeEnum.NONE.getType(),
                ImContentTypeEnum.FRIEND_REQUEST_REJECTED.getType(), payload);
    }

    @Override
    public List<ImFriendRequestDO> getMyFriendRequestList(Long userId, Long maxId, Integer limit) {
        ImFriendRequestDO maxRequest = maxId != null ? friendRequestMapper.selectById(maxId) : null;
        if (maxId != null && maxRequest == null) {
            return ListUtil.of();
        }
        return friendRequestMapper.selectMyList(userId,
                maxRequest != null ? maxRequest.getUpdateTime() : null,
                maxRequest != null ? maxRequest.getId() : null,
                limit);
    }

    @Override
    public List<ImFriendRequestDO> pullFriendRequestList(Long userId, Long lastUpdateTime, Long lastId, Integer limit) {
        return friendRequestMapper.selectPullListByUserId(userId, lastUpdateTime, lastId, limit);
    }

    @Override
    public ImFriendRequestDO getFriendRequest(Long id) {
        return friendRequestMapper.selectById(id);
    }

    @Override
    public PageResult<ImFriendRequestDO> getFriendRequestPage(ImFriendRequestManagerPageReqVO reqVO) {
        return friendRequestMapper.selectPage(reqVO);
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
