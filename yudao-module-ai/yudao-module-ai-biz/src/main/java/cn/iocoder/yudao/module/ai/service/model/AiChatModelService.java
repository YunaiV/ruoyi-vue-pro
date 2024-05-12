package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatModel.AiChatModelPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatModel.AiChatModelSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import jakarta.validation.Valid;

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
     * @return API 聊天模型
     */
    AiChatModelDO getChatModel(Long id);

    /**
     * 获得聊天模型分页
     *
     * @param pageReqVO 分页查询
     * @return API 聊天模型分页
     */
    PageResult<AiChatModelDO> getChatModelPage(AiChatModelPageReqVO pageReqVO);

    /**
     * 校验聊天模型
     *
     * @param id 编号
     * @return 聊天模型
     */
    AiChatModelDO validateChatModel(Long id);

}
