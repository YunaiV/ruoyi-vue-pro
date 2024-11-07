package cn.iocoder.yudao.module.promotion.service.kefu;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.conversation.KeFuConversationUpdatePinnedReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuConversationDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuMessageDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.kefu.KeFuConversationMapper;
import cn.iocoder.yudao.module.promotion.enums.kefu.KeFuMessageContentTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.KEFU_CONVERSATION_NOT_EXISTS;

/**
 * 客服会话 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class KeFuConversationServiceImpl implements KeFuConversationService {

    @Resource
    private KeFuConversationMapper conversationMapper;

    @Override
    public void deleteKefuConversation(Long id) {
        // 校验存在
        validateKefuConversationExists(id);

        // 只有管理员端可以删除会话，也不真的删，只是管理员端看不到啦
        conversationMapper.updateById(new KeFuConversationDO().setId(id).setAdminDeleted(Boolean.TRUE));
    }

    @Override
    public void updateConversationPinnedByAdmin(KeFuConversationUpdatePinnedReqVO updateReqVO) {
        // 校验存在
        validateKefuConversationExists(updateReqVO.getId());

        // 更新管理员会话置顶状态
        conversationMapper.updateById(new KeFuConversationDO().setId(updateReqVO.getId()).setAdminPinned(updateReqVO.getAdminPinned()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConversationLastMessage(KeFuMessageDO kefuMessage) {
        // 1.1 校验会话是否存在
        KeFuConversationDO conversation = validateKefuConversationExists(kefuMessage.getConversationId());
        // 1.2 更新会话消息冗余
        conversationMapper.updateById(new KeFuConversationDO().setId(kefuMessage.getConversationId())
                .setLastMessageTime(kefuMessage.getCreateTime()).setLastMessageContent(kefuMessage.getContent())
                .setLastMessageContentType(kefuMessage.getContentType()));

        // 2.1 更新管理员未读消息数
        if (UserTypeEnum.MEMBER.getValue().equals(kefuMessage.getSenderType())) {
            conversationMapper.updateAdminUnreadMessageCountIncrement(kefuMessage.getConversationId());
        }
        // 2.2 会员用户发送消息时，如果管理员删除过会话则进行恢复
        if (Boolean.TRUE.equals(conversation.getAdminDeleted())) {
            updateConversationAdminDeleted(kefuMessage.getConversationId(), Boolean.FALSE);
        }
    }

    @Override
    public void updateAdminUnreadMessageCountToZero(Long id) {
        // 校验存在
        validateKefuConversationExists(id);

        // 管理员未读消息数归零
        conversationMapper.updateById(new KeFuConversationDO().setId(id).setAdminUnreadMessageCount(0));
    }

    @Override
    public void updateConversationAdminDeleted(Long id, Boolean adminDeleted) {
        conversationMapper.updateById(new KeFuConversationDO().setId(id).setAdminDeleted(adminDeleted));
    }

    @Override
    public List<KeFuConversationDO> getKefuConversationList() {
        return conversationMapper.selectConversationList();
    }

    @Override
    public KeFuConversationDO getOrCreateConversation(Long userId) {
        KeFuConversationDO conversation = conversationMapper.selectOne(KeFuConversationDO::getUserId, userId);
        // 没有历史会话，则初始化一个新会话
        if (conversation == null) {
            conversation = new KeFuConversationDO().setUserId(userId).setLastMessageTime(LocalDateTime.now())
                    .setLastMessageContent(StrUtil.EMPTY).setLastMessageContentType(KeFuMessageContentTypeEnum.TEXT.getType())
                    .setAdminPinned(Boolean.FALSE).setUserDeleted(Boolean.FALSE).setAdminDeleted(Boolean.FALSE)
                    .setAdminUnreadMessageCount(0);
            conversationMapper.insert(conversation);
        }
        return conversation;
    }

    @Override
    public KeFuConversationDO validateKefuConversationExists(Long id) {
        KeFuConversationDO conversation = conversationMapper.selectById(id);
        if (conversation == null) {
            throw exception(KEFU_CONVERSATION_NOT_EXISTS);
        }
        return conversation;
    }

    @Override
    public KeFuConversationDO getConversationByUserId(Long userId) {
        return conversationMapper.selectByUserId(userId);
    }

}