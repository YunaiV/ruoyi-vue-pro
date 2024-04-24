package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.vo.*;

import java.util.List;

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
    PageResult<ChatRoleListRes> list(ChatRoleListReq req);

    /**
     * chat角色 - 添加
     *
     * @param req
     * @return
     */
    void add(ChatRoleAddReq req);

    /**
     * chat角色 - 修改
     *
     * @param id
     * @param req
     */
    void update(Long id, ChatRoleUpdateReq req);


    /**
     * chat角色 - 修改可见性
     *
     * @param id
     * @param req
     */
    void updateVisibility(Long id, ChatRoleUpdateVisibilityReq req);

    /**
     * chat角色 - 删除
     *
     * @param chatRoleId
     */
    void delete(Long chatRoleId);

}
