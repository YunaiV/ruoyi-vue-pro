package cn.iocoder.yudao.module.promotion.service.kefu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.api.websocket.WebSocketSenderApi;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessagePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessageSendReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.kefu.vo.message.AppKeFuMessagePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.kefu.vo.message.AppKeFuMessageSendReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuConversationDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuMessageDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.kefu.KeFuMessageMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.KEFU_CONVERSATION_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.WebSocketMessageTypeConstants.KEFU_MESSAGE_ADMIN_READ;
import static cn.iocoder.yudao.module.promotion.enums.WebSocketMessageTypeConstants.KEFU_MESSAGE_TYPE;

/**
 * 客服消息 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class KeFuMessageServiceImpl implements KeFuMessageService {

    @Resource
    private KeFuMessageMapper keFuMessageMapper;
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
        validateReceiverExist(conversation.getUserId(), UserTypeEnum.MEMBER.getValue());

        // 2.1 保存消息
        KeFuMessageDO kefuMessage = BeanUtils.toBean(sendReqVO, KeFuMessageDO.class);
        kefuMessage.setReceiverId(conversation.getUserId()).setReceiverType(UserTypeEnum.MEMBER.getValue()); // 设置接收人
        keFuMessageMapper.insert(kefuMessage);
        // 2.2 更新会话消息冗余
        conversationService.updateConversationLastMessage(kefuMessage);

        // 3. 发送消息
        getSelf().sendAsyncMessage(UserTypeEnum.MEMBER.getValue(), conversation.getUserId(), kefuMessage);
        return kefuMessage.getId();
    }

    @Override
    public Long sendKefuMessage(AppKeFuMessageSendReqVO sendReqVO) {
        // 1.1 设置会话编号
        KeFuMessageDO kefuMessage = BeanUtils.toBean(sendReqVO, KeFuMessageDO.class);
        KeFuConversationDO conversation = conversationService.getOrCreateConversation(sendReqVO.getSenderId());
        kefuMessage.setConversationId(conversation.getId());
        // 1.2 保存消息
        keFuMessageMapper.insert(kefuMessage);

        // 2. 更新会话消息冗余
        conversationService.updateConversationLastMessage(kefuMessage);
        // 3. 发送消息
        getSelf().sendAsyncMessageToAdmin(kefuMessage);
        return kefuMessage.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateKeFuMessageReadStatus(Long conversationId, Long userId, Integer userType) {
        // 1.1 校验会话是否存在
        KeFuConversationDO conversation = conversationService.validateKefuConversationExists(conversationId);
        // 1.2 如果是会员端处理已读，需要传递 userId；万一用户模拟一个 conversationId
        if (UserTypeEnum.MEMBER.getValue().equals(userType) && ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(KEFU_CONVERSATION_NOT_EXISTS);
        }
        // 1.2 查询会话所有的未读消息 (tips: 多个客服，一个人点了，就都点了)
        List<KeFuMessageDO> messageList = keFuMessageMapper.selectListByConversationIdAndReadStatus(conversationId, Boolean.FALSE);
        // 1.3 情况一：没有未读消息
        if (CollUtil.isEmpty(messageList)) {
            return;
        }

        // 2.1 情况二：更新未读消息状态为已读
        keFuMessageMapper.updateReadStatusBatchByIds(convertSet(messageList, KeFuMessageDO::getId),
                new KeFuMessageDO().setReadStatus(Boolean.TRUE));
        // 2.2 将管理员未读消息计数更新为零
        conversationService.updateAdminUnreadMessageCountWithZero(conversationId);

        // 2.3 发送消息通知会员，管理员已读 -> 会员更新发送的消息状态
        // TODO @puhui999：待定~
        KeFuMessageDO keFuMessage = getFirst(filterList(messageList, message -> UserTypeEnum.MEMBER.getValue().equals(message.getSenderType())));
        assert keFuMessage != null; // 断言避免警告
        webSocketSenderApi.sendObject(UserTypeEnum.MEMBER.getValue(), keFuMessage.getSenderId(), KEFU_MESSAGE_ADMIN_READ, StrUtil.EMPTY);
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

    @Async
    public void sendAsyncMessageToAdmin(Object content) {
        webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), KEFU_MESSAGE_TYPE, content);
    }

    @Override
    public PageResult<KeFuMessageDO> getKeFuMessagePage(KeFuMessagePageReqVO pageReqVO) {
        return keFuMessageMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<KeFuMessageDO> getKeFuMessagePage(AppKeFuMessagePageReqVO pageReqVO, Long userId) {
        // 1. 获得客服会话
        KeFuConversationDO conversation = conversationService.getConversationByUserId(userId);
        if (conversation == null) {
            return PageResult.empty();
        }
        // 2. 设置会话编号
        pageReqVO.setConversationId(conversation.getId());
        return keFuMessageMapper.selectPage(pageReqVO);
    }

    private KeFuMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}