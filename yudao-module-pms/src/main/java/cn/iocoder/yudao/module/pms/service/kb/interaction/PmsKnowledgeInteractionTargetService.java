package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.PmsKnowledgeInteractionItemRespVO;

import java.util.Collection;
import java.util.List;

/**
 * PMS 知识互动对象 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeInteractionTargetService {

    /**
     * 校验互动对象可读
     *
     * @param type 对象类型
     * @param entityId 对象编号
     * @param userId 用户编号
     * @return 知识库编号
     */
    Long validateTargetReadable(Integer type, Long entityId, Long userId);

    /**
     * 批量获得当前用户可读的互动对象
     *
     * @param targets 互动对象集合
     * @param userId 用户编号
     * @return 可读的互动对象列表
     */
    List<PmsKnowledgeInteractionItemRespVO> getReadableItemList(
            Collection<PmsKnowledgeInteractionItemRespVO> targets, Long userId);

}
