package cn.iocoder.yudao.module.business.hi.task.inst.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.bpm.domain.vo.ApproveProcInstVO;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.de.odysseus.el.util.SimpleContext;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * [  ]
 *
 * @author 孟凯
 * @version 1.0
 * @company 1024创新实验室(www.1024lab.net)
 * @copyright (c)  1024创新实验室( www.1024lab.net )Inc. All rights reserved.
 * @date 2022-01-17 15:14:27
 * @since JDK1.8
 */
@Slf4j
@Service
public class HiTaskinstService {


    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;

    /**
     * 获取任务具体流程信息
     *
     * @param procInstId 流程id
     * @param procDefId  流程部署id
     *
     * @return 返回流程信息
     */
    public List<ApproveProcInstVO> taskGetComment(String procInstId, String procDefId) {
        Map<String, Object> variableMap = new HashMap<>(50);
        List<HistoricVariableInstance> hisVarInstList =
            historyService.createHistoricVariableInstanceQuery().processInstanceId(procInstId).list();
        hisVarInstList.forEach(hisVarInst -> {
            variableMap.put(hisVarInst.getName(), hisVarInst.getValue());
        });

        List<ApproveProcInstVO> procInstVOList = hiTaskinstDao.listByProcInstIdAndNotDelete(procInstId);

        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModel(procDefId);
        DomElement document1 = bpmnModelInstance.getDocument().getElementById(procDefId.split(":")[0]);
        LinkedHashMap<String, DomElement> domElementMap = new LinkedHashMap<>(50);
        getChildElementToMap(document1, domElementMap);
        getChildNode(procInstVOList, domElementMap, variableMap);
        return procInstVOList;
    }

    /**
     * 获取子节点信息，存储到map中
     *
     * @param domElement    父节点
     * @param domElementMap 节点map
     */
    private void getChildElementToMap(DomElement domElement, LinkedHashMap<String, DomElement> domElementMap) {
        List<DomElement> childElements = domElement.getChildElements();
        for (DomElement childElement : childElements) {
            if ("BPMNDiagram".equals(childElement.getLocalName())) {
                break;
            }
            if ("extensionElements".equals(childElement.getLocalName())) {
                break;
            }
            if (!"outgoing".equals(childElement.getLocalName()) && !"incoming".equals(childElement.getLocalName())) {
                domElementMap.put(childElement.getAttribute("id"), childElement);
            }
            getChildElementToMap(childElement, domElementMap);
        }
    }

    /**
     * 获取子节点信息
     *
     * @param procInstVOList 流程VO列表
     * @param domElementMap  节点map
     * @param variableMap    流程所有的变量
     */
    private void getChildNode(List<ApproveProcInstVO> procInstVOList, LinkedHashMap<String, DomElement> domElementMap,
        Map<String, Object> variableMap) {
        DomElement domElement = domElementMap.get(procInstVOList.get(procInstVOList.size() - 1).getTaskDefKey());
        for (DomElement childElement : domElement.getChildElements()) {
            if ("outgoing".equals(childElement.getLocalName())) {
                DomElement tmpDomElement = domElementMap.get(childElement.getTextContent());
                getChildNode(procInstVOList, domElementMap, variableMap, tmpDomElement);
            }
        }
    }

    /**
     * 获取子节点信息
     *
     * @param procInstVOList 流程VO列表
     * @param domElementMap  节点map
     * @param variableMap    流程所有的变量
     * @param domElement     父节点信息
     */
    private void getChildNode(List<ApproveProcInstVO> procInstVOList, LinkedHashMap<String, DomElement> domElementMap,
        Map<String, Object> variableMap, DomElement domElement) {
        if ("exclusiveGateway".equals(domElement.getLocalName())) {
            DomElement domElementChild = getNodeExclusiveGateway(domElementMap, variableMap, domElement);
            getChildNode(procInstVOList, domElementMap, variableMap, domElementChild);
        } else if ("userTask".equals(domElement.getLocalName())) {
            getProcInstVO(procInstVOList, domElement, variableMap);
            for (DomElement childElement : domElement.getChildElements()) {
                if ("outgoing".equals(childElement.getLocalName())) {
                    DomElement tmpDomElement = domElementMap.get(childElement.getTextContent());
                    DomElement domElementChild = getNodeBySequenceFlow(domElementMap, tmpDomElement);
                    if ("endEvent".equals(domElementChild.getLocalName())) {
                        return;
                    }
                    getChildNode(procInstVOList, domElementMap, variableMap, domElementChild);
                }
            }
        } else {
            DomElement domElementChild = getNodeBySequenceFlow(domElementMap, domElement);
            if (BeanUtil.isEmpty(domElementChild)) {
                throw new RuntimeException("无流程可执行！！！");
            }
            if ("endEvent".equals(domElementChild.getLocalName())) {
                return;
            }
            getChildNode(procInstVOList, domElementMap, variableMap, domElementChild);
        }
    }

    /**
     * 获取网关节点的子节点
     *
     * @param domElementMap 流程所有的节点
     * @param variableMap   流程所有的变量
     * @param domElement    父节点信息
     *
     * @return 返回子节点信息
     */
    private DomElement getNodeExclusiveGateway(LinkedHashMap<String, DomElement> domElementMap,
        Map<String, Object> variableMap, DomElement domElement) {
        Boolean elExpressionFlag = Boolean.FALSE;
        ArrayList<DomElement> nullChildSequenceFlowList = new ArrayList<>();
        for (DomElement exclusiveGatewayChild : domElement.getChildElements()) {
            if ("outgoing".equals(exclusiveGatewayChild.getLocalName()) && BooleanUtil.isFalse(elExpressionFlag)) {
                DomElement sequenceFlowDomElement = domElementMap.get(exclusiveGatewayChild.getTextContent());
                if (CollUtil.isNotEmpty(sequenceFlowDomElement.getChildElements())
                    && sequenceFlowDomElement.getChildElements().size() > 0) {
                    for (DomElement conditionExpression : sequenceFlowDomElement.getChildElements()) {
                        if ("conditionExpression".equals(conditionExpression.getLocalName())) {
                            elExpressionFlag = elExpression(conditionExpression.getTextContent(), variableMap);
                            if (BooleanUtil.isTrue(elExpressionFlag)) {
                                break;
                            }
                        }
                    }
                } else {
                    nullChildSequenceFlowList.add(sequenceFlowDomElement);
                }
                if (elExpressionFlag) {
                    return getNodeBySequenceFlow(domElementMap, sequenceFlowDomElement);
                }
            }
        }
        if (CollUtil.isEmpty(nullChildSequenceFlowList) && nullChildSequenceFlowList.size() < 1) {
            throw new RuntimeException("网关缺少无条件流程可执行！！！");
        }
        if (nullChildSequenceFlowList.size() > 1) {
            throw new RuntimeException("无条件流程大于1条！！！");
        }
        return getNodeBySequenceFlow(domElementMap, nullChildSequenceFlowList.get(0));
    }

    /**
     * 获取流程序列流的子节点
     *
     * @param domElementMap 流程所有节点
     * @param domElement    父节点信息
     *
     * @return 返回子节点
     */
    private DomElement getNodeBySequenceFlow(LinkedHashMap<String, DomElement> domElementMap, DomElement domElement) {
        String targetRef = domElement.getAttribute("targetRef");
        return domElementMap.get(targetRef);
    }

    /**
     * 设置节点信息进ApproveProcInstVO
     *
     * @param procInstVOList ApproveProcInstVOList
     * @param domElement     节点信息
     * @param variableMap    流程所有变量
     */
    private void getProcInstVO(List<ApproveProcInstVO> procInstVOList, DomElement domElement,
        Map<String, Object> variableMap) {
        String camundaNameSpaceUri = "http://camunda.org/schema/1.0/bpmn";
        boolean userListFlag = false;
        List<Object> approveProcInstVOList = new ArrayList<>();
        for (DomElement childElement : domElement.getChildElements()) {
            if ("multiInstanceLoopCharacteristics".equals(childElement.getLocalName())) {
                userListFlag = true;
                String collectionVar = childElement.getAttribute(camundaNameSpaceUri, "collection");
                Object taskUserList = variableMap.get(collectionVar);
                approveProcInstVOList = JSONUtil.parseArray(taskUserList);
            }
        }

        if (userListFlag) {
            approveProcInstVOList.forEach(tmpAssignee -> {
                String assignee = String.valueOf(tmpAssignee);
                ApproveProcInstVO approveProcInstVO = new ApproveProcInstVO();
                approveProcInstVO.setName(domElement.getAttribute("name"));
                approveProcInstVO.setTaskDefKey(domElement.getAttribute("id"));
                approveProcInstVO.setAssignee(assignee);
                approveProcInstVO.setAssigneeName(employeeDao.getByLoginName(assignee, 0).getActualName());
                procInstVOList.add(approveProcInstVO);
            });
        } else {
            ApproveProcInstVO approveProcInstVO = new ApproveProcInstVO();
            approveProcInstVO.setName(domElement.getAttribute("name"));
            approveProcInstVO.setTaskDefKey(domElement.getAttribute("id"));
            approveProcInstVO.setAssignee(domElement.getAttribute(camundaNameSpaceUri, "assignee"));
            approveProcInstVO.setAssigneeName(
                employeeDao.getByLoginName(approveProcInstVO.getAssignee(), 0).getActualName());
            procInstVOList.add(approveProcInstVO);
        }
    }

    /**
     * 网关分叉条件判断，网关分叉，必须要有默认出口
     *
     * @param elExpression el表达式
     * @param variableMap  流程所有变量
     *
     * @return 返回true或false
     */
    private Boolean elExpression(String elExpression, Map<String, Object> variableMap) {
        ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();
        for (String k : variableMap.keySet()) {
            if (variableMap.get(k) != null) {
                context.setVariable(k,
                    factory.createValueExpression(variableMap.get(k), variableMap.get(k).getClass()));
            }
        }
        ValueExpression e = factory.createValueExpression(context, elExpression, Boolean.class);
        //el表达式和variables得到的结果
        return (Boolean)e.getValue(context);
    }
}
