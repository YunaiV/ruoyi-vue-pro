package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole.AiChatRolePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole.AiChatRoleSaveMyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole.AiChatRoleSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

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
     * 创建【我的】聊天角色
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 编号
     */
    Long createChatRoleMy(AiChatRoleSaveMyReqVO createReqVO, Long userId);

    /**
     * 更新聊天角色
     *
     * @param updateReqVO 更新信息
     */
    void updateChatRole(@Valid AiChatRoleSaveReqVO updateReqVO);

    /**
     * 创建【我的】聊天角色
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateChatRoleMy(AiChatRoleSaveMyReqVO updateReqVO, Long userId);

    /**
     * 删除聊天角色
     *
     * @param id 编号
     */
    void deleteChatRole(Long id);

    /**
     * 删除【我的】聊天角色
     *
     * @param id 编号
     * @param userId 用户编号
     */
    void deleteChatRoleMy(Long id, Long userId);

    /**
     * 获得聊天角色
     *
     * @param id 编号
     * @return AI 聊天角色
     */
    AiChatRoleDO getChatRole(Long id);

    /**
     * 获得聊天角色列表
     *
     * @param ids 编号数组
     * @return 聊天角色列表
     */
    List<AiChatRoleDO> getChatRoleList(Collection<Long> ids);

    default Map<Long, AiChatRoleDO> getChatRoleMap(Collection<Long> ids) {
        return convertMap(getChatRoleList(ids), AiChatRoleDO::getId);
    }

    /**
     * 校验聊天角色是否合法
     *
     * @param id 角色编号
     */
    AiChatRoleDO validateChatRole(Long id);

    /**
     * 获得聊天角色分页
     *
     * @param pageReqVO 分页查询
     * @return 聊天角色分页
     */
    PageResult<AiChatRoleDO> getChatRolePage(AiChatRolePageReqVO pageReqVO);

    /**
     * 获得【我的】聊天角色分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @return 聊天角色分页
     */
    PageResult<AiChatRoleDO> getChatRoleMyPage(AiChatRolePageReqVO pageReqVO, Long userId);

    /**
     * 获得聊天角色的分类列表
     *
     * @return 分类列表
     */
    List<String> getChatRoleCategoryList();

    /**
     * 根据名字获得聊天角色
     *
     * @param name 名字
     * @return 聊天角色列表
     */
    List<AiChatRoleDO> getChatRoleListByName(String name);

}