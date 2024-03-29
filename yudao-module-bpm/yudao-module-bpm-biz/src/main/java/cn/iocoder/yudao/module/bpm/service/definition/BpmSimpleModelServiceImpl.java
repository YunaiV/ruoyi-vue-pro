package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.simple.BpmSimpleModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeType;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.*;
import org.flowable.engine.repository.Model;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.CONVERT_TO_SIMPLE_MODEL_NOT_SUPPORT;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.MODEL_NOT_EXISTS;
import static cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeType.START_NODE;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.USER_TASK_CANDIDATE_PARAM;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY;

/**
 * 仿钉钉流程设计 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class BpmSimpleModelServiceImpl implements BpmSimpleModelService {
    @Resource
    private BpmModelService bpmModelService;

    @Override
    public Boolean saveSimpleModel(BpmSimpleModelSaveReqVO reqVO) {
        Model model = bpmModelService.getModel(reqVO.getModelId());
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
//        byte[] bpmnBytes = bpmModelService.getModelBpmnXML(reqVO.getModelId());
//        if (ArrayUtil.isEmpty(bpmnBytes)) {
//            //  BPMN XML 不存在。新增
//            BpmnModel bpmnModel = BpmnModelUtils.convertSimpleModelToBpmnModel(model.getKey(), model.getName(), reqVO.getSimpleModelBody());
//            bpmModelService.saveModelBpmnXml(model.getId(), BpmnModelUtils.getBpmnXml(bpmnModel));
//            return Boolean.TRUE;
//        } else {
//            // TODO BPMN XML 已经存在。如何修改 ??
//            return Boolean.FALSE;
//        }
        // 暂时直接修改
        BpmnModel bpmnModel = BpmnModelUtils.convertSimpleModelToBpmnModel(model.getKey(), model.getName(), reqVO.getSimpleModelBody());
        bpmModelService.saveModelBpmnXml(model.getId(), BpmnModelUtils.getBpmnXml(bpmnModel));
        return Boolean.TRUE;
    }

    @Override
    public BpmSimpleModelNodeVO getSimpleModel(String modelId) {
        Model model = bpmModelService.getModel(modelId);
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        byte[] bpmnBytes = bpmModelService.getModelBpmnXML(modelId);
        BpmnModel bpmnModel = BpmnModelUtils.getBpmnModel(bpmnBytes);
        return convertBpmnModelToSimpleModel(bpmnModel);

    }

    /**
     * Bpmn Model 转换成 仿钉钉流程设计模型数据结构(json) 待完善
     *
     * @param bpmnModel Bpmn Model
     * @return 仿钉钉流程设计模型数据结构
     */
    private BpmSimpleModelNodeVO convertBpmnModelToSimpleModel(BpmnModel bpmnModel) {
        if (bpmnModel == null) {
            return null;
        }
        StartEvent startEvent = BpmnModelUtils.getStartEvent(bpmnModel);
        if (startEvent == null) {
            return null;
        }
        BpmSimpleModelNodeVO rootNode = new BpmSimpleModelNodeVO();
        rootNode.setType(START_NODE.getType());
        rootNode.setId(startEvent.getId());
        rootNode.setName(startEvent.getName());
        recursiveBuildSimpleModelNode(startEvent, rootNode);
        return rootNode;

    }

    private void recursiveBuildSimpleModelNode(FlowNode currentFlowNode, BpmSimpleModelNodeVO currentSimpleModeNode) {
        BpmSimpleModelNodeType nodeType = BpmSimpleModelNodeType.valueOf(currentSimpleModeNode.getType());
        Assert.notNull(nodeType, "节点类型不支持");
        // 校验节点是否支持转仿钉钉的流程模型
        List<SequenceFlow> outgoingFlows = validateCanConvertSimpleNode(nodeType, currentFlowNode);
        if (CollUtil.isEmpty(outgoingFlows) || outgoingFlows.get(0).getTargetFlowElement() == null) {
            return;
        }
        FlowElement targetElement = outgoingFlows.get(0).getTargetFlowElement();
        // 如果是 EndEvent 直接退出
        if (targetElement instanceof EndEvent) {
            return;
        }
        if (targetElement instanceof UserTask) {
            BpmSimpleModelNodeVO childNode = convertUserTaskToSimpleModelNode((UserTask) targetElement);
            currentSimpleModeNode.setChildNode(childNode);
            recursiveBuildSimpleModelNode((FlowNode) targetElement, childNode);
        }
        // TODO 其它节点类型待实现
    }


    private BpmSimpleModelNodeVO convertUserTaskToSimpleModelNode(UserTask userTask) {
        BpmSimpleModelNodeVO simpleModelNodeVO = new BpmSimpleModelNodeVO();
        simpleModelNodeVO.setType(BpmSimpleModelNodeType.APPROVE_USER_NODE.getType());
        simpleModelNodeVO.setName(userTask.getName());
        simpleModelNodeVO.setId(userTask.getId());
        Map<String, Object> attributes = MapUtil.newHashMap();
        // TODO 暂时是普通审批，需要加会签
        attributes.put("approveMethod", 1);
        attributes.computeIfAbsent(USER_TASK_CANDIDATE_STRATEGY, (key) -> BpmnModelUtils.parseCandidateStrategy(userTask));
        attributes.computeIfAbsent(USER_TASK_CANDIDATE_PARAM, (key) -> BpmnModelUtils.parseCandidateParam(userTask));
        simpleModelNodeVO.setAttributes(attributes);
        return simpleModelNodeVO;
    }

    private List<SequenceFlow> validateCanConvertSimpleNode(BpmSimpleModelNodeType nodeType, FlowNode currentFlowNode) {
        switch (nodeType) {
            case START_NODE:
            case APPROVE_USER_NODE: {
                List<SequenceFlow> outgoingFlows = currentFlowNode.getOutgoingFlows();
                if (CollUtil.isNotEmpty(outgoingFlows) && outgoingFlows.size() > 1) {
                    throw exception(CONVERT_TO_SIMPLE_MODEL_NOT_SUPPORT);
                }
                validIsSupportFlowNode(outgoingFlows.get(0).getTargetFlowElement());
                return outgoingFlows;
            }
            default: {
                // TODO 其它节点类型待实现
                throw exception(CONVERT_TO_SIMPLE_MODEL_NOT_SUPPORT);
            }
        }
    }

    private void validIsSupportFlowNode(FlowElement targetElement) {
        if (targetElement == null) {
            return;
        }
        boolean isSupport = false;
        for (Class<? extends FlowNode> item : BpmnModelConstants.SUPPORT_CONVERT_SIMPLE_FlOW_NODES) {
            if (item.isInstance(targetElement)) {
                isSupport = true;
                break;
            }
        }
        if (!isSupport) {
            throw exception(CONVERT_TO_SIMPLE_MODEL_NOT_SUPPORT);
        }
    }
}
