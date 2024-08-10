package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatModel.AiChatModelPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatModel.AiChatModelSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

import java.util.Set;

/**
 * AI 聊天模型 Service 接口
 *
 * @author fansili
 * @since 2024/4/24 19:42
 */
public interface AiChatModelService {

    /**
     * 创建聊天模型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createChatModel(@Valid AiChatModelSaveReqVO createReqVO);

    /**
     * 更新聊天模型
     *
     * @param updateReqVO 更新信息
     */
    void updateChatModel(@Valid AiChatModelSaveReqVO updateReqVO);

    /**
     * 删除聊天模型
     *
     * @param id 编号
     */
    void deleteChatModel(Long id);

    /**
     * 获得聊天模型
     *
     * @param id 编号
     * @return 聊天模型
     */
    AiChatModelDO getChatModel(Long id);

    /**
     * 获得默认的聊天模型
     *
     * 如果获取不到，则抛出 {@link cn.iocoder.yudao.framework.common.exception.ServiceException} 业务异常
     *
     * @return 聊天模型
     */
    AiChatModelDO getRequiredDefaultChatModel();

    /**
     * 获得聊天模型分页
     *
     * @param pageReqVO 分页查询
     * @return 聊天模型分页
     */
    PageResult<AiChatModelDO> getChatModelPage(AiChatModelPageReqVO pageReqVO);

    /**
     * 校验聊天模型
     *
     * @param id 编号
     * @return 聊天模型
     */
    AiChatModelDO validateChatModel(Long id);

    /**
     * 获得聊天模型列表
     *
     * @param status 状态
     * @return 聊天模型列表
     */
    List<AiChatModelDO> getChatModelListByStatus(Integer status);

    /**
     * 获得聊天模型列表
     *
     * @param ids 编号数组
     * @return 模型列表
     */
    List<AiChatModelDO> getChatModelList(Collection<Long> ids);
}
