package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionPageItemRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionExtDO;
import cn.iocoder.yudao.module.bpm.service.definition.dto.BpmProcessDefinitionCreateReqDTO;

import javax.validation.Valid;

/**
 * 流程定义通用接口
 * Activiti 和 flowable 通用的流程定义接口
 *
 * @author yunlong.li
 * @author ZJQ
 * @author 芋道源码
 * @author jason
 */
public interface BpmProcessDefinitionCommonService {

    /**
     * 获得流程定义分页
     *
     * @param pageReqVO 分页入参
     * @return 流程定义 Page
     */
    PageResult<BpmProcessDefinitionPageItemRespVO> getProcessDefinitionPage(BpmProcessDefinitionPageReqVO pageReqVO);

    /**
     * 创建流程定义
     *
     * @param createReqDTO 创建信息
     * @return 流程编号
     */
    String createProcessDefinition(@Valid BpmProcessDefinitionCreateReqDTO createReqDTO);

    /**
     * 更新流程定义状态
     *
     * @param id 流程定义的编号
     * @param state 状态
     */
    void updateProcessDefinitionState(String id, Integer state);

    /**
     * 获得流程定义对应的 BPMN XML
     *
     * @param id 流程定义编号
     * @return BPMN XML
     */
    String getProcessDefinitionBpmnXML(String id);

    /**
     * 获得需要创建的流程定义，是否和当前激活的流程定义相等
     *
     * @param createReqDTO 创建信息
     * @return 是否相等
     */
    boolean isProcessDefinitionEquals(@Valid BpmProcessDefinitionCreateReqDTO createReqDTO);

    /**
     * 获得编号对应的 BpmProcessDefinitionExtDO
     *
     * @param id 编号
     * @return 流程定义拓展
     */
    BpmProcessDefinitionExtDO getProcessDefinitionExt(String id);
}
