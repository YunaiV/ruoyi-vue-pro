package cn.iocoder.yudao.adminserver.modules.bpm.service.definition;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.rule.BpmTaskAssignRuleCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.rule.BpmTaskAssignRuleRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.rule.BpmTaskAssignRuleUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmTaskAssignRuleDO;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.util.List;

/**
 * BPM 任务分配规则 Service 接口
 *
 * @author 芋道源码
 */
public interface BpmTaskAssignRuleService {

    /**
     * 获得流程定义的任务分配规则数组
     *
     * @param processDefinitionId 流程定义的编号
     * @param taskDefinitionKey 流程任务定义的 Key。允许空
     * @return 任务规则数组
     */
    List<BpmTaskAssignRuleDO> getTaskAssignRuleListByProcessDefinitionId(String processDefinitionId,
                                                                         @Nullable String taskDefinitionKey);

    /**
     * 获得流程模型的任务规则数组
     *
     * @param modelId 流程模型的编号
     * @return 任务规则数组
     */
    List<BpmTaskAssignRuleDO> getTaskAssignRuleListByModelId(String modelId);


    /**
     * 获得流程定义的任务分配规则数组
     *
     * @param modelId 流程模型的编号
     * @param processDefinitionId 流程定义的编号
     * @return 任务规则数组
     */
    List<BpmTaskAssignRuleRespVO> getTaskAssignRuleList(String modelId, String processDefinitionId);

    /**
     * 创建任务分配规则
     *
     * @param reqVO 创建信息
     * @return 规则编号
     */
    Long createTaskAssignRule(@Valid BpmTaskAssignRuleCreateReqVO reqVO);

    /**
     * 更新任务分配规则
     *
     * @param reqVO 创建信息
     */
    void updateTaskAssignRule(@Valid BpmTaskAssignRuleUpdateReqVO reqVO);

}
