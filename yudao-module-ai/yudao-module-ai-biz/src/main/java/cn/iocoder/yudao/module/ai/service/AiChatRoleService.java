package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.role.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;

/**
 * chat 角色
 *
 * @fansili
 * @since v1.0
 */
public interface AiChatRoleService {

    /**
     * 获取聊天角色列表
     *
     * @param req
     * @return
     */
    PageResult<AiChatRoleListRes> list(AiChatRoleListReq req);

    /**
     * chat角色 - 添加
     *
     * @param req
     * @return
     */
    void add(AiChatRoleAddReqVO req);

    /**
     * chat角色 - 修改
     *
     * @param req
     */
    void update(AiChatRoleUpdateReqVO req);


    /**
     * chat角色 - 修改可见性
     *
     * @param req
     */
    void updatePublicStatus(AiChatRoleUpdatePublicStatusReqVO req);

    /**
     * chat角色 - 删除
     *
     * @param chatRoleId
     */
    void delete(Long chatRoleId);

    /**
     * 获取角色
     *
     * @param roleId
     * @return
     */
    AiChatRoleRes getChatRole(Long roleId);

    /**
     * 校验 - 角色是否存在
     *
     * @param id
     * @return
     */
    AiChatRoleDO validateExists(Long id);

    /**
     * 校验 - 角色是否公开
     *
     * @param aiChatRoleDO
     */
    void validateIsPublic(AiChatRoleDO aiChatRoleDO);
}
