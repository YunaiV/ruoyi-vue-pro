package cn.iocoder.yudao.adminserver.modules.bpm.service.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition.ProcessDefinitionPageReqVo;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition.ProcessDefinitionRespVO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 流程定义接口
 * @author yunlong.li
 */
public interface BpmProcessDefinitionService {
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
}
