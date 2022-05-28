package cn.iocoder.yudao.module.business.hi.task.inst.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskTodoPageItemRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmTaskAssignRuleDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmTaskExtDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmTaskAssignRuleMapper;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.DeptMapper;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.impl.de.odysseus.el.ExpressionFactoryImpl;
import org.flowable.common.engine.impl.de.odysseus.el.util.SimpleContext;
import org.flowable.common.engine.impl.javax.el.ExpressionFactory;
import org.flowable.common.engine.impl.javax.el.ValueExpression;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * [  ]
 *
 * @author 孟凯
 * @version 1.0
 */
@Slf4j
@Service
public class HiTaskInstService {

    @Resource
    private HistoryService historyService;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private BpmTaskAssignRuleMapper bpmTaskAssignRuleMapper;
    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private AdminUserMapper adminUserApi;
    @Resource
    private DeptMapper deptMapper;

    /**
     * 获取任务具体流程信息
     *
     * @param taskList 任务信息
     * @param approved 任意选择条件
     *
     * @return 返回流程信息
     */
    @TenantIgnore
    public List<BpmTaskRespVO> taskGetComment(List<BpmTaskExtDO> taskList, Object approved) {
        BpmTaskExtDO task = taskList.get(taskList.size() - 1);
        Map<String, BpmTaskExtDO> bpmTaskMap =
            taskList.stream().collect(Collectors.toMap(BpmTaskExtDO::getTaskId, Function.identity()));
        // 获得 ProcessInstance Map
        HistoricProcessInstance procInst =
            processInstanceService.getHistoricProcessInstance(task.getProcessInstanceId());
        Map<Long, AdminUserDO> userDoMap =
            adminUserApi.selectList().stream().collect(Collectors.toMap(AdminUserDO::getId, Function.identity()));
        Map<Long, DeptDO> deptMap =
            deptMapper.selectList().stream().collect(Collectors.toMap(DeptDO::getId, Function.identity()));
        List<HistoricTaskInstance> hisTaskInstList =
            historyService.createHistoricTaskInstanceQuery().processInstanceId(task.getProcessInstanceId())
                .orderByHistoricTaskInstanceStartTime().desc().list();
        LinkedList<BpmTaskRespVO> bpmTaskRespVOList =
            nowTaskFormat(procInst, hisTaskInstList, bpmTaskMap, userDoMap, deptMap);

        //ProcessInstanceId流程实例
        String procInstId = task.getProcessInstanceId();
        List<BpmTaskAssignRuleDO> tmpBpmTaskAssignRuleDOList =
            bpmTaskAssignRuleMapper.selectListByProcessDefinitionId(task.getProcessDefinitionId(), null);
        List<HistoricVariableInstance> hisVarInstList =
            historyService.createHistoricVariableInstanceQuery().processInstanceId(procInstId).list();
        Map<String, Object> hisVarInstMap = new HashMap<>();
        for (HistoricVariableInstance hisVarInst : hisVarInstList) {
            hisVarInstMap.put(hisVarInst.getVariableName(), hisVarInst.getValue());
        }
        hisVarInstMap.put("approved", approved);
        //获取bpm（模型）对象
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        getFlow(bpmnModel, task.getTaskDefKey(), tmpBpmTaskAssignRuleDOList, bpmTaskRespVOList, userDoMap, deptMap,
            hisVarInstMap);
        return bpmTaskRespVOList;
    }

    /**
     * 格式化任务信息
     */
    private LinkedList<BpmTaskRespVO> nowTaskFormat(HistoricProcessInstance procInst, List<HistoricTaskInstance> taskList,
        Map<String, BpmTaskExtDO> bpmTaskExtDoMap, Map<Long, AdminUserDO> userMap, Map<Long, DeptDO> deptMap) {
        LinkedList<BpmTaskRespVO> bpmTaskRespVOList = new LinkedList<>();
        for (HistoricTaskInstance hisTaskInst : taskList) {
            BpmTaskRespVO respVO = initRespVo(hisTaskInst, bpmTaskExtDoMap, procInst, userMap);
            AdminUserDO user = userMap.get(Long.valueOf(hisTaskInst.getAssignee()));
            if (BeanUtil.isNotEmpty(user)) {
                respVO.setAssigneeUser(setUser(user));
                DeptDO dept = deptMap.get(user.getDeptId());
                if (BeanUtil.isNotEmpty(dept)) {
                    respVO.getAssigneeUser().setDeptName(dept.getName());
                }
            }
            bpmTaskRespVOList.addFirst(respVO);
        }
        return bpmTaskRespVOList;
    }

    private BpmTaskRespVO initRespVo(Object taskInst, Map<String, BpmTaskExtDO> bpmTaskExtDoMap,
        HistoricProcessInstance procInst, Map<Long, AdminUserDO> userMap) {
        BpmTaskRespVO respVO = new BpmTaskRespVO();
        if (taskInst instanceof HistoricTaskInstance) {
            respVO = setBpmnTaskRespVo((HistoricTaskInstance)taskInst);
            BeanUtil.copyProperties(taskInst, respVO);
            BpmTaskExtDO bpmTaskExtDO = bpmTaskExtDoMap.get(respVO.getId());
            if (ObjectUtil.isNotEmpty(bpmTaskExtDO)) {
                BeanUtil.copyProperties(bpmTaskExtDO, respVO);
                respVO.setId(bpmTaskExtDO.getTaskId());
            }
        }
        if (taskInst instanceof BpmTaskExtDO) {
            respVO = setBpmnTaskRespVo((BpmTaskExtDO)taskInst);
            BeanUtil.copyProperties(taskInst, respVO);
        }
        //            copyTo(bpmTaskExtDO, respVO);
        if (procInst != null) {
            AdminUserDO startUser = userMap.get(Long.valueOf(procInst.getStartUserId()));
            if (BeanUtil.isEmpty(startUser)) {
                throw new RuntimeException("查找不到审批用户!!!");
            }
            respVO.setProcessInstance(setProcInst(procInst, startUser));
        }
        return respVO;
    }

    private BpmTaskRespVO.User setUser(AdminUserDO bean) {
        if (bean == null) {
            return null;
        }

        BpmTaskRespVO.User user = new BpmTaskRespVO.User();

        user.setId(bean.getId());
        user.setNickname(bean.getNickname());
        user.setDeptId(bean.getDeptId());

        return user;

    }

    private BpmTaskRespVO setBpmnTaskRespVo(BpmTaskExtDO bean) {
        if (bean == null) {
            return null;
        }

        BpmTaskRespVO bpmTaskRespVO = new BpmTaskRespVO();

        bpmTaskRespVO.setDefinitionKey(bean.getTaskDefKey());
        bpmTaskRespVO.setId(bean.getTaskId());
        bpmTaskRespVO.setName(bean.getName());
        bpmTaskRespVO.setCreateTime(bean.getCreateTime());

        return bpmTaskRespVO;
    }

    public BpmTaskRespVO setBpmnTaskRespVo(HistoricTaskInstance bean) {
        if (bean == null) {
            return null;
        }

        BpmTaskRespVO bpmTaskRespVO = new BpmTaskRespVO();

        bpmTaskRespVO.setDefinitionKey(bean.getTaskDefinitionKey());
        bpmTaskRespVO.setId(bean.getId());
        bpmTaskRespVO.setName(bean.getName());
        bpmTaskRespVO.setClaimTime(bean.getClaimTime());
        bpmTaskRespVO.setCreateTime(bean.getCreateTime());
        bpmTaskRespVO.setEndTime(bean.getEndTime());
        bpmTaskRespVO.setDurationInMillis(bean.getDurationInMillis());

        return bpmTaskRespVO;
    }

    public BpmTaskTodoPageItemRespVO.ProcessInstance setProcInst(HistoricProcessInstance processInstance,
        AdminUserDO startUser) {
        if (processInstance == null && startUser == null) {
            return null;
        }

        BpmTaskTodoPageItemRespVO.ProcessInstance processInstanceResult =
            new BpmTaskTodoPageItemRespVO.ProcessInstance();

        if (processInstance != null) {
            processInstanceResult.setId(processInstance.getId());
            processInstanceResult.setName(processInstance.getName());
            if (processInstance.getStartUserId() != null) {
                processInstanceResult.setStartUserId(Long.parseLong(processInstance.getStartUserId()));
            }
            processInstanceResult.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        }
        if (startUser != null) {
            processInstanceResult.setStartUserNickname(startUser.getNickname());
        }

        return processInstanceResult;
    }

    private void getFlow(BpmnModel bpmnModel, String taskDefKey, List<BpmTaskAssignRuleDO> tmpBpmTaskAssignRuleDOList,
        LinkedList<BpmTaskRespVO> bpmTaskRespVOList, Map<Long, AdminUserDO> userDoMap, Map<Long, DeptDO> deptMap,
        Map<String, Object> taskVarMap) {
        //传节点定义key获取当前节点
        FlowNode flowNode = (FlowNode)bpmnModel.getFlowElement(taskDefKey);
        //输出连线
        List<SequenceFlow> outgoingFlows = flowNode.getOutgoingFlows();
        //遍历返回下一个节点信息
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            //类型自己判断（获取下个节点是网关还是节点）
            FlowElement targetFlowElement = outgoingFlow.getTargetFlowElement();
            getFlow(bpmnModel, targetFlowElement, tmpBpmTaskAssignRuleDOList, bpmTaskRespVOList, userDoMap, deptMap,
                taskVarMap);
        }
    }

    private void getFlow(BpmnModel bpmnModel, FlowElement targetFlowElement,
        List<BpmTaskAssignRuleDO> tmpBpmTaskAssignRuleDOList, LinkedList<BpmTaskRespVO> bpmTaskRespVOList,
        Map<Long, AdminUserDO> userDoMap, Map<Long, DeptDO> deptMap, Map<String, Object> taskVarMap) {
        // 下一个taskDefKey
        String nextTaskDefKey = null;
        //下个是节点
        if (targetFlowElement instanceof UserTask) {
            // 判断是否是为并行任务
            List<BpmTaskAssignRuleDO> bpmTaskAssignRuleDOList = tmpBpmTaskAssignRuleDOList.stream().filter(
                    bpmTaskAssignRuleDO -> bpmTaskAssignRuleDO.getTaskDefinitionKey().equals(targetFlowElement.getId()))
                .collect(Collectors.toList());
            if (CollUtil.isEmpty(bpmTaskAssignRuleDOList)) {
                throw new RuntimeException("任务key不存在!!!");
            }
            for (BpmTaskAssignRuleDO bpmTaskAssignRuleDO : bpmTaskAssignRuleDOList) {
                nextTaskDefKey = bpmTaskAssignRuleDO.getTaskDefinitionKey();
                for (Long userId : bpmTaskAssignRuleDO.getOptions()) {
                    BpmTaskRespVO bpmTaskRespVO =
                        (BpmTaskRespVO)new BpmTaskRespVO().setName(targetFlowElement.getName());
                    bpmTaskRespVOList.addLast(bpmTaskRespVO);
                    AdminUserDO adminUserDO = userDoMap.get(userId);
                    DeptDO deptDO = deptMap.get(adminUserDO.getDeptId());
                    bpmTaskRespVO.setAssigneeUser(setUser(adminUserDO));
                    bpmTaskRespVO.getAssigneeUser().setDeptName(deptDO.getName());
                    // edit by 芋艿
//                    if (!bpmTaskAssignRuleDO.getType().equals(BpmTaskAssignRuleTypeEnum.USER_OR_SIGN.getType())
//                        && !bpmTaskAssignRuleDO.getType().equals(BpmTaskAssignRuleTypeEnum.USER_SIGN.getType())) {
//                        break;
//                    }
                }
            }
            getFlow(bpmnModel, nextTaskDefKey, tmpBpmTaskAssignRuleDOList, bpmTaskRespVOList, userDoMap, deptMap,
                taskVarMap);
            // 下个节点是网关（调用下面的方法）
        } else if (targetFlowElement instanceof ExclusiveGateway) {
            String defaultFlow = ((ExclusiveGateway)targetFlowElement).getDefaultFlow();
            FlowElement nexFlowElement = getExclusiveGateway(targetFlowElement, taskVarMap, defaultFlow);
            getFlow(bpmnModel, nexFlowElement, tmpBpmTaskAssignRuleDOList, bpmTaskRespVOList, userDoMap, deptMap,
                taskVarMap);
        }
    }

    /**
     * 获取排他网关分支名称、分支表达式、下一级任务节点
     *
     * @param flowElement 任务节点
     * @param taskVarMap  审批数据
     */
    private FlowElement getExclusiveGateway(FlowElement flowElement, Map<String, Object> taskVarMap,
        String defaultFlow) {
        // 获取所有网关分支
        List<SequenceFlow> targetFlows = ((ExclusiveGateway)flowElement).getOutgoingFlows();
        Boolean elExpressionFlag = Boolean.FALSE;
        FlowElement sequenceFlowResult = null;
        FlowElement defaultSequenceFlow = null;
        // 循环每个网关分支
        for (SequenceFlow sequenceFlow : targetFlows) {
            if (defaultFlow.equals(sequenceFlow.getId())) {
                defaultSequenceFlow = sequenceFlow.getTargetFlowElement();
                continue;
            }
            elExpressionFlag = elExpression(sequenceFlow.getConditionExpression(), taskVarMap);
            if (elExpressionFlag) {
                // 获取下一个网关和节点数据
                FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
                // 网关的下个节点是用户节点
                if (targetFlowElement instanceof UserTask) {
                    sequenceFlowResult = targetFlowElement;
                    break;
                } else if (targetFlowElement instanceof EndEvent) {
                    log.info("排他网关的下一节点是EndEvent: 结束节点");
                } else if (targetFlowElement instanceof ServiceTask) {
                    log.info("排他网关的下一节点是ServiceTask: 内部方法");
                } else if (targetFlowElement instanceof ExclusiveGateway) {
                    defaultFlow = ((ExclusiveGateway)targetFlowElement).getDefaultFlow();
                    return getExclusiveGateway(targetFlowElement, taskVarMap, defaultFlow);
                } else if (targetFlowElement instanceof SubProcess) {
                    log.info("排他网关的下一节点是SubProcess: 内部子流程");
                }
            }
        }
        if (!elExpressionFlag) {
            if (defaultSequenceFlow instanceof UserTask) {
                sequenceFlowResult = defaultSequenceFlow;
            } else if (defaultSequenceFlow instanceof ExclusiveGateway) {
                defaultFlow = ((ExclusiveGateway)defaultSequenceFlow).getDefaultFlow();
                return getExclusiveGateway(defaultSequenceFlow, taskVarMap, defaultFlow);
            }
        }
        return sequenceFlowResult;
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
