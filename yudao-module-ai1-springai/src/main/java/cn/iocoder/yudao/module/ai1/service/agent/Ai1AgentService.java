package cn.iocoder.yudao.module.ai1.service.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.agent.vo.Ai1AgentPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.agent.vo.Ai1AgentSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * AI1 Agent Service 接口
 *
 * @author 芋道源码
 */
public interface Ai1AgentService {

    /**
     * 创建 Agent，初始为关闭
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAgent(@Valid Ai1AgentSaveReqVO createReqVO);

    /**
     * 更新 Agent；状态不在此修改
     *
     * @param updateReqVO 更新信息
     */
    void updateAgent(@Valid Ai1AgentSaveReqVO updateReqVO);

    /**
     * 删除 Agent，级联删除会话与消息，并清理 SKILL 物化沙箱
     *
     * @param id 编号
     */
    void deleteAgent(Long id);

    /**
     * 批量删除 Agent，级联删除会话与消息，并清理 SKILL 物化沙箱
     *
     * @param ids 编号列表
     */
    void deleteAgentListByIds(List<Long> ids);

    /**
     * 获得 Agent
     *
     * @param id 编号
     * @return Agent
     */
    Ai1AgentDO getAgent(Long id);

    /**
     * 校验 Agent 是否存在
     *
     * @param id 编号
     * @return Agent
     */
    Ai1AgentDO validateAgentExists(Long id);

    /**
     * 获得 Agent 分页
     *
     * @param pageReqVO 分页查询
     * @return Agent 分页
     */
    PageResult<Ai1AgentDO> getAgentPage(Ai1AgentPageReqVO pageReqVO);

    /**
     * 校验 Agent 存在且已开启
     *
     * @param id 编号
     * @return Agent
     */
    Ai1AgentDO validateAgentEnabled(Long id);

    /**
     * 获得指定状态的 Agent 列表，用于会话页选择 Agent
     *
     * @param status 状态
     * @return Agent 列表
     */
    List<Ai1AgentDO> getAgentListByStatus(Integer status);

    /**
     * 获得 Agent 列表
     *
     * @param ids 编号集合
     * @return Agent 列表
     */
    List<Ai1AgentDO> getAgentList(Collection<Long> ids);

    /**
     * 获得 Agent Map
     *
     * @param ids 编号集合
     * @return Agent Map
     */
    default Map<Long, Ai1AgentDO> getAgentMap(Collection<Long> ids) {
        return convertMap(getAgentList(ids), Ai1AgentDO::getId);
    }

    /**
     * 获得使用指定模型的 Agent 数量
     *
     * @param modelIds 模型编号集合
     * @return Agent 数量
     */
    Long getAgentCountByModelIds(Collection<Long> modelIds);

    /**
     * 修改 Agent 状态
     *
     * @param id     Agent 编号
     * @param status 状态
     */
    void updateAgentStatus(Long id, Integer status);

}
