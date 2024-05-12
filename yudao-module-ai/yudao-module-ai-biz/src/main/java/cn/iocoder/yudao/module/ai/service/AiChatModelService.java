package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;

import java.util.List;
import java.util.Set;

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
     * @param req
     */
    void update(AiChatModelUpdateReqVO req);

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

    /**
     * 获取 - 根据 ids 批量获取
     *
     * @param modalIds
     * @return
     */
    List<AiChatModelDO> getModalByIds(Set<Long> modalIds);
}
