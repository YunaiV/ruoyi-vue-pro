package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModelAddReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModelListReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModelListRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;

/**
 * ai modal
 *
 * @author fansili
 * @time 2024/4/24 19:42
 * @since 1.0
 */
public interface AiChatModelService {

    /**
     * ai modal - 列表
     *
     * @param req
     * @return
     */
    PageResult<AiChatModelListRespVO> list(AiChatModelListReqVO req);

    /**
     * ai modal - 添加
     *
     * @param req
     */
    void add(AiChatModelAddReqVO req);

    /**
     * ai modal - 更新
     *
     * @param id
     * @param req
     */
    void update(Long id, AiChatModelAddReqVO req);

    /**
     * ai modal - 删除
     *
     * @param id
     */
    void delete(Long id);

    /**
     * 获取 - 获取 modal
     *
     * @param modalId
     * @return
     */
    AiChatModalRespVO getChatModalOfValidate(Long modalId);

    /**
     * 校验 - 是否存在
     *
     * @param id
     * @return
     */
    AiChatModelDO validateExists(Long id);

    /**
     * 校验 - 校验是否可用
     *
     * @param chatModal
     */
    void validateAvailable(AiChatModalRespVO chatModal);
}
