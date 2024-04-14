package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.app.chat.vo.*;

/**
 * chat 角色
 *
 * @fansili
 * @since v1.0
 */
public interface ChatRoleService {

    /**
     * 获取聊天角色列表
     *
     * @param req
     * @return
     */
    CommonResult<ChatRoleListRes> list(ChatRoleListReq req);

    /**
     * chat角色 - 添加
     *
     * @param req
     * @return
     */
    ChatRoleListRes add(ChatRoleAddReq req);

    /**
     * chat角色 - 修改
     *
     * @param req
     */
    void update(ChatRoleUpdateReq req);


    /**
     * chat角色 - 修改可见性
     *
     * @param req
     */
    void updateVisibility(ChatRoleUpdateVisibilityReq req);

    /**
     * chat角色 - 删除
     *
     * @param chatRoleId
     */
    void delete(Long chatRoleId);

}
