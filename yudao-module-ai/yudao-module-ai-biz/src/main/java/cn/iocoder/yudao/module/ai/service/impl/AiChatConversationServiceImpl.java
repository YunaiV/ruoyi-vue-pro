package cn.iocoder.yudao.module.ai.service.impl;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationRespVO;
import cn.iocoder.yudao.module.ai.convert.AiChatConversationConvert;
import cn.iocoder.yudao.module.ai.enums.AiChatConversationTypeEnum;
import cn.iocoder.yudao.module.ai.enums.AiChatModalDisableEnum;
import cn.iocoder.yudao.module.ai.mapper.AiChatConversationMapper;
import cn.iocoder.yudao.module.ai.mapper.AiChatModalMapper;
import cn.iocoder.yudao.module.ai.mapper.AiChatRoleMapper;
import cn.iocoder.yudao.module.ai.service.AiChatConversationService;
import cn.iocoder.yudao.module.ai.service.AiChatModalService;
import cn.iocoder.yudao.module.ai.service.AiChatRoleService;
import cn.iocoder.yudao.module.ai.vo.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * chat 对话
 *
 * @fansili
 * @since v1.0
 */
@Service
@Slf4j
@AllArgsConstructor
public class AiChatConversationServiceImpl implements AiChatConversationService {

    private final AiChatRoleMapper aiChatRoleMapper;
    private final AiChatModalMapper aiChatModalMapper;
    private final AiChatConversationMapper aiChatConversationMapper;
    private final AiChatModalService aiChatModalService;
    private final AiChatRoleService aiChatRoleService;

    @Override
    public AiChatConversationRespVO createConversation(AiChatConversationCreateUserReq req) {
        // 获取用户id
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 查询最新的对话
        AiChatConversationDO latestConversation = aiChatConversationMapper.selectLatestConversation(loginUserId);
        // 如果有对话没有被使用过，那就返回这个
        if (latestConversation != null && latestConversation.getChatCount() <= 0) {
            return AiChatConversationConvert.INSTANCE.covnertChatConversationRes(latestConversation);
        }
        // 获取第一个模型
        AiChatModalDO aiChatModalDO = aiChatModalMapper.selectFirstModal();
        // 创建新的 Conversation
        AiChatConversationDO insertConversation = saveConversation(req.getTitle(), loginUserId,
                null, null, AiChatConversationTypeEnum.USER_CHAT,
                aiChatModalDO.getId(), aiChatModalDO.getModal());
        // 转换 res
        return AiChatConversationConvert.INSTANCE.covnertChatConversationRes(insertConversation);
    }

    @Override
    public AiChatConversationRespVO createRoleConversation(AiChatConversationCreateReqVO req) {
        // 获取用户id
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 查询最新的对话
//        AiChatConversationDO latestConversation = aiChatConversationMapper.selectLatestConversation(loginUserId);
//        // 如果有对话没有被使用过，那就返回这个
//        if (latestConversation != null && latestConversation.getChatCount() <= 0) {
//            return AiChatConversationConvert.INSTANCE.covnertChatConversationRes(latestConversation);
//        }
        // 查询角色
        AiChatRoleRes chatRoleRes = aiChatRoleService.getChatRole(req.getRoleId());
        // 获取第一个模型
        AiChatModalDO aiChatModalDO = aiChatModalMapper.selectFirstModal();
        // 创建新的 Conversation
        AiChatConversationDO insertConversation = saveConversation(req.getTitle(), loginUserId,
                req.getRoleId(), chatRoleRes.getName(), AiChatConversationTypeEnum.ROLE_CHAT,
                aiChatModalDO.getId(), aiChatModalDO.getModal());
        // 转换 res
        return AiChatConversationConvert.INSTANCE.covnertChatConversationRes(insertConversation);
    }

    private @NotNull AiChatConversationDO saveConversation(String title,
                                                           Long userId,
                                                           Long roleId,
                                                           String roleName,
                                                           AiChatConversationTypeEnum typeEnum,
                                                           Long modalId,
                                                           String modal) {
        AiChatConversationDO insertConversation = new AiChatConversationDO();
        insertConversation.setId(null);
        insertConversation.setUserId(userId);
        insertConversation.setRoleId(roleId);
        insertConversation.setRoleName(roleName);
        insertConversation.setTitle(title);
        insertConversation.setChatCount(0);
        insertConversation.setType(typeEnum.getType());
        insertConversation.setModalId(modalId);
        insertConversation.setModal(modal);
        aiChatConversationMapper.insert(insertConversation);
        return insertConversation;
    }

    @Override
    public AiChatConversationRespVO getConversation(Long id) {
        AiChatConversationDO aiChatConversationDO = validateExists(id);
        return AiChatConversationConvert.INSTANCE.covnertChatConversationRes(aiChatConversationDO);
    }

    private @NotNull AiChatConversationDO validateExists(Long id) {
        AiChatConversationDO aiChatConversationDO = aiChatConversationMapper.selectById(id);
        if (aiChatConversationDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_CHAT_CONTINUE_NOT_EXIST);
        }
        return aiChatConversationDO;
    }

    @Override
    public List<AiChatConversationRespVO> listConversation(AiChatConversationListReq req) {
        // 获取用户id
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 查询前100对话
        List<AiChatConversationDO> top100Conversation
                = aiChatConversationMapper.selectTop100Conversation(loginUserId, req.getSearch());
        return AiChatConversationConvert.INSTANCE.covnertChatConversationResList(top100Conversation);
    }

    @Override
    public void updateModal(Long id, Long modalId) {
        // 校验对话是否存在
        validateExists(id);
        // 获取模型
        AiChatModalRes chatModal = aiChatModalService.getChatModal(modalId);
        // 判断模型是否禁用
        if (AiChatModalDisableEnum.YES.getValue().equals(chatModal.getDisable())) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MODAL_DISABLE_NOT_USED);
        }
        // 更新对话
        aiChatConversationMapper.updateById(new AiChatConversationDO()
                .setId(id)
                .setModalId(chatModal.getId())
                .setModal(chatModal.getModal())
        );
    }

    @Override
    public void delete(Long id) {
        aiChatConversationMapper.deleteById(id);
    }
}
