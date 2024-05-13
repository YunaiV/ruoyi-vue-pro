package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole.AiChatRolePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole.AiChatRoleSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import jakarta.validation.Valid;

/**
 * AI 聊天角色 Service 接口
 *
 * @author fansili
 */
public interface AiChatRoleService {

    /**
     * 创建聊天角色
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createChatRole(@Valid AiChatRoleSaveReqVO createReqVO);

    /**
     * 更新聊天角色
     *
     * @param updateReqVO 更新信息
     */
    void updateChatRole(@Valid AiChatRoleSaveReqVO updateReqVO);

    /**
     * 删除聊天角色
     *
     * @param id 编号
     */
    void deleteChatRole(Long id);

    /**
     * 获得聊天角色
     *
     * @param id 编号
     * @return AI 聊天角色
     */
    AiChatRoleDO getChatRole(Long id);

    /**
     * 校验角色是否存在
     *
     * @param id 角色编号
     */
    AiChatRoleDO validateChatRole(Long id);

    /**
     * 获得AI 聊天角色分页
     *
     * @param pageReqVO 分页查询
     * @return AI 聊天角色分页
     */
    PageResult<AiChatRoleDO> getChatRolePage(AiChatRolePageReqVO pageReqVO);

}