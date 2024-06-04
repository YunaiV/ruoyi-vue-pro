package cn.iocoder.yudao.module.promotion.service.kefu;

import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.conversation.KeFuConversationUpdatePinnedReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuConversationDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.kefu.KeFuConversationMapper;
import cn.iocoder.yudao.module.promotion.enums.kehu.KeFuMessageContentTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
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
    public void updatePinned(KeFuConversationUpdatePinnedReqVO updateReqVO) {
        // 只有管理员端可以置顶会话
        conversationMapper.updateById(new KeFuConversationDO().setId(updateReqVO.getId()).setAdminPinned(updateReqVO.getAdminPinned()));
    }

    @Override
    public void updateConversationMessage(Long id, LocalDateTime lastMessageTime, String lastMessageContent, Integer lastMessageContentType) {
        conversationMapper.updateById(new KeFuConversationDO().setId(id).setLastMessageTime(lastMessageTime)
                .setLastMessageContent(lastMessageContent).setLastMessageContentType(lastMessageContentType));
    }

    @Override
    public void updateAdminUnreadMessageCountByConversationId(Long id, Integer count) {
        conversationMapper.updateAdminUnreadMessageCountByConversationId(id, count);
    }

    @Override
    public void updateConversationAdminDeleted(Long id, Boolean adminDeleted) {
        conversationMapper.updateById(new KeFuConversationDO().setId(id).setAdminDeleted(adminDeleted));
    }

    @Override
    public List<KeFuConversationDO> getKefuConversationList() {
        return conversationMapper.selectListWithSort();
    }

    @Override
    public KeFuConversationDO getOrCreateConversation(Long userId) {
        KeFuConversationDO conversation = conversationMapper.selectOne(KeFuConversationDO::getUserId, userId);
        if (conversation == null) { // 没有历史会话则初始化一个新会话
            conversation = new KeFuConversationDO().setUserId(userId).setLastMessageTime(LocalDateTime.now())
                    .setLastMessageContent("").setLastMessageContentType(KeFuMessageContentTypeEnum.TEXT.getType())
                    .setAdminPinned(Boolean.FALSE).setUserDeleted(Boolean.FALSE).setAdminDeleted(Boolean.FALSE)
                    .setAdminUnreadMessageCount(0);
            conversationMapper.insert(conversation);
        }
        return conversation;
    }

    @Override
    public KeFuConversationDO validateKefuConversationExists(Long id) {
        KeFuConversationDO conversationDO = conversationMapper.selectById(id);
        if (conversationDO == null) {
            throw exception(KEFU_CONVERSATION_NOT_EXISTS);
        }

        return conversationDO;
    }

}