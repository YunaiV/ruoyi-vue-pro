package cn.iocoder.yudao.adminserver.modules.bpm.service.definition;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition.ProcessDefinitionPageReqVo;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition.ProcessDefinitionRespVO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.activiti.engine.repository.ProcessDefinition;

import java.util.List;
import java.util.Set;

/**
 * 流程定义接口
 *
 * @author yunlong.li
 */
public interface BpmDefinitionService {

    /**
     * 流程定义分页
     * @param processDefinitionPageReqVo 分页入参
     * @return 分页model
     */
    PageResult<ProcessDefinitionRespVO> pageList(ProcessDefinitionPageReqVo processDefinitionPageReqVo);

    /**
     * 导出流程 bpmn 模型
     * @param processDefinitionId 分页入参
     * @return 分页model
     */
    FileResp export(String processDefinitionId);

    /**
     * 获得 deploymentId 对应的 ProcessDefinition 数组
     *
     * @param deploymentId 部署编号
     * @return 流程定义的数组
     */
    List<ProcessDefinition> getProcessDefinitionListByDeploymentIds(Set<String> deploymentId);

}
