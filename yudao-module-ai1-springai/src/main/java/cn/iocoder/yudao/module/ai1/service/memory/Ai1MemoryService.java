package cn.iocoder.yudao.module.ai1.service.memory;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.memory.vo.Ai1MemoryPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.memory.vo.Ai1MemoryUpdateReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.memory.Ai1MemoryDO;
import javax.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * AI1 长期记忆 Service 接口
 *
 * @author 芋道源码
 */
public interface Ai1MemoryService {

    /**
     * 修改长期记忆正文
     *
     * @param updateReqVO 修改信息
     */
    void updateMemory(@Valid Ai1MemoryUpdateReqVO updateReqVO);

    /**
     * 删除长期记忆（逻辑删除）；之后的归档会根据历史消息生成新的记录
     *
     * @param id 编号
     */
    void deleteMemory(Long id);

    /**
     * 批量删除长期记忆
     *
     * @param ids 编号列表
     */
    void deleteMemoryListByIds(List<Long> ids);

    /**
     * 删除 Agent 下的全部长期记忆（Agent 删除时调用）
     *
     * @param agentIds Agent 编号集合
     */
    void deleteMemoryListByAgentIds(Collection<Long> agentIds);

    /**
     * 获得长期记忆
     *
     * @param id 编号
     * @return 长期记忆
     */
    Ai1MemoryDO getMemory(Long id);

    /**
     * 获得用户在 Agent 下的长期记忆
     *
     * @param agentId Agent 编号
     * @param userId  用户编号
     * @return 长期记忆
     */
    Ai1MemoryDO getMemoryByAgentIdAndUserId(Long agentId, Long userId);

    /**
     * 创建归档的长期记忆；写入前校验，Agent 已删除，或同一用户已有未删除的记忆时不创建
     *
     * @param memory 长期记忆
     * @return 是否创建成功
     */
    boolean createMemoryIfAgentExists(Ai1MemoryDO memory);

    /**
     * 更新归档的长期记忆；写入前校验，读取后被人工修改或删除时不更新
     *
     * @param old           归档读取时的记忆
     * @param content       新的记忆正文
     * @param lastMessageId 新的水位线
     * @return 是否更新成功
     */
    boolean updateMemoryIfUnchanged(Ai1MemoryDO old, String content, Long lastMessageId);

    /**
     * 获得长期记忆分页
     *
     * @param pageReqVO 分页查询
     * @return 长期记忆分页
     */
    PageResult<Ai1MemoryDO> getMemoryPage(Ai1MemoryPageReqVO pageReqVO);

}
