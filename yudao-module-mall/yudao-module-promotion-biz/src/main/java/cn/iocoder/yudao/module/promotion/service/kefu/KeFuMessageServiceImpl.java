package cn.iocoder.yudao.module.promotion.service.kefu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.api.websocket.WebSocketSenderApi;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessagePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessageSendReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuConversationDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuMessageDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.kefu.KeFuMessageMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getFirst;

/**
 * 客服消息 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class KeFuMessageServiceImpl implements KeFuMessageService {

    private static final String KEFU_MESSAGE_TYPE = "kefu_message_type"; // 客服消息类型

    @Resource
    private KeFuMessageMapper messageMapper;
    @Resource
    private KeFuConversationService conversationService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private WebSocketSenderApi webSocketSenderApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendKefuMessage(KeFuMessageSendReqVO sendReqVO) {
        // 1.1 校验会话是否存在
        KeFuConversationDO conversation = conversationService.validateKefuConversationExists(sendReqVO.getConversationId());
        // 1.2 校验接收人是否存在
        validateReceiverExist(sendReqVO.getReceiverId(), sendReqVO.getReceiverType());

        // 2.1 保存消息
        KeFuMessageDO kefuMessage = BeanUtils.toBean(sendReqVO, KeFuMessageDO.class);
        messageMapper.insert(kefuMessage);
        // 2.2 更新会话消息冗余
        conversationService.updateConversationMessage(kefuMessage.getConversationId(), LocalDateTime.now(),
                kefuMessage.getContent(), kefuMessage.getContentType());
        // 2.3 更新管理员未读消息数
        if (UserTypeEnum.ADMIN.getValue().equals(kefuMessage.getReceiverType())) {
            conversationService.updateAdminUnreadMessageCountByConversationId(kefuMessage.getConversationId(), 1);
        }
        // 2.4 会员用户发送消息时，如果管理员删除过会话则进行恢复
        if (UserTypeEnum.MEMBER.getValue().equals(kefuMessage.getSenderType()) && Boolean.TRUE.equals(conversation.getAdminDeleted())) {
            conversationService.updateConversationAdminDeleted(kefuMessage.getConversationId(), Boolean.FALSE);
        }

        // 3. 发送消息
        getSelf().sendAsyncMessage(sendReqVO.getReceiverType(), sendReqVO.getReceiverId(), kefuMessage);

        // 返回
        return kefuMessage.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateKefuMessageReadStatus(Long conversationId, Long receiverId) {
        // 1.1 校验会话是否存在
        conversationService.validateKefuConversationExists(conversationId);
        // 1.2 查询接收人所有的未读消息
        List<KeFuMessageDO> messageList = messageMapper.selectListByConversationIdAndReceiverIdAndReadStatus(
                conversationId, receiverId, Boolean.FALSE);
        // 1.3 情况一：没有未读消息
        if (CollUtil.isEmpty(messageList)) {
            return;
        }

        // 2.1 情况二：更新未读消息状态为已读
        messageMapper.updateReadStstusBatchByIds(convertSet(messageList, KeFuMessageDO::getId), Boolean.TRUE);
        // 2.2 更新管理员未读消息数
        KeFuMessageDO message = getFirst(messageList);
        assert message != null;
        if (UserTypeEnum.ADMIN.getValue().equals(message.getReceiverType())) {
            conversationService.updateAdminUnreadMessageCountByConversationId(conversationId, 0);
        }
        // 2.3 发送消息通知发送者，接收者已读 -> 发送者更新发送的消息状态
        getSelf().sendAsyncMessage(message.getSenderType(), message.getSenderId(), "keFuMessageReadStatusChange");
    }

    private void validateReceiverExist(Long receiverId, Integer receiverType) {
        if (UserTypeEnum.ADMIN.getValue().equals(receiverType)) {
            adminUserApi.validateUser(receiverId);
        }
        if (UserTypeEnum.MEMBER.getValue().equals(receiverType)) {
            memberUserApi.validateUser(receiverId);
        }
    }

    @Async
    public void sendAsyncMessage(Integer userType, Long userId, Object content) {
        webSocketSenderApi.sendObject(userType, userId, KEFU_MESSAGE_TYPE, content);
    }

    @Override
    public PageResult<KeFuMessageDO> getKefuMessagePage(KeFuMessagePageReqVO pageReqVO) {
        return messageMapper.selectPage(pageReqVO);
    }

    private KeFuMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}