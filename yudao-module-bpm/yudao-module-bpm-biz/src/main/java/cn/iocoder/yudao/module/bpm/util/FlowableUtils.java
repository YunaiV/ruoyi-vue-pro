package cn.iocoder.yudao.module.bpm.util;


import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.List;
import java.util.Map;

/**
 * 流程引擎工具类封装
 *
 * @author: linjinp
 * @create: 2019-12-24 13:51
 **/
public class FlowableUtils {

    /**
     * 获取流程名称
     *
     * @param processDefinitionId
     * @return
     */
    public static String getFlowName(String processDefinitionId) {
        RepositoryService repositoryService = SpringUtil.getBean(RepositoryService.class);
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
        return processDefinition.getName();
    }

    /**
     * 获取节点数据
     *
     * @param processInstanceId
     * @param nodeId
     * @return
     */
    public static FlowNode getFlowNode(String processInstanceId, String nodeId) {

        RuntimeService runtimeService = SpringUtil.getBean(RuntimeService.class);
        RepositoryService repositoryService = SpringUtil.getBean(RepositoryService.class);

        String definitionld = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();        //获取bpm（模型）对象
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definitionld);
        //传节点定义key获取当前节点
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(nodeId);
        return flowNode;
    }

    public static ExtensionElement generateFlowNodeIdExtension(String nodeId) {
        ExtensionElement extensionElement = new ExtensionElement();
        extensionElement.setElementText(nodeId);
        extensionElement.setName("nodeId");
        extensionElement.setNamespacePrefix("flowable");
        extensionElement.setNamespace("nodeId");
        return extensionElement;
    }

    public static String getNodeIdFromExtension(FlowElement flowElement) {
        Map<String, List<ExtensionElement>> extensionElements = flowElement.getExtensionElements();
        return extensionElements.get("nodeId").get(0).getElementText();
    }

    public static Long getStartUserIdFromProcessInstance(ProcessInstance instance) {
        if (null == instance) {
            return null;
        }
        return NumberUtils.parseLong(instance.getStartUserId());
    }

}
