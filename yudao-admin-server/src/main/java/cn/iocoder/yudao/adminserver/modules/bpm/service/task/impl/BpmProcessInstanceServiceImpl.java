package cn.iocoder.yudao.adminserver.modules.bpm.service.task.impl;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstanceCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.adminserver.modules.system.service.user.SysUserService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Map;

import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.PROCESS_DEFINITION_IS_SUSPENDED;
import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.PROCESS_DEFINITION_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 流程实例 Service 实现类
 *
 * ProcessDefinition & ProcessInstance & Execution & Task 的关系：
 *     1. https://blog.csdn.net/bobozai86/article/details/105210414
 *
 * HistoricProcessInstance & ProcessInstance 的关系：
 *     1.https://my.oschina.net/843294669/blog/719024
 * 简单来说，前者 = 历史 + 运行中的流程实例，后者仅是运行中的流程实例
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class BpmProcessInstanceServiceImpl implements BpmProcessInstanceService {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;
    @Resource
    private HistoryService historyService;

    @Resource
    private SysUserService userService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Override
    public String createProcessInstance(Long userId, BpmProcessInstanceCreateReqVO createReqVO) {
        // 校验流程定义
        ProcessDefinition definition = processDefinitionService.getProcessDefinition(createReqVO.getProcessDefinitionId());
        if (definition == null) {
            throw exception(PROCESS_DEFINITION_NOT_EXISTS);
        }
        if (definition.isSuspended()) {
            throw exception(PROCESS_DEFINITION_IS_SUSPENDED);
        }

        // 设置流程发起人
        Authentication.setAuthenticatedUserId(String.valueOf(userId));

        // 创建流程实例
        Map<String, Object> variables = createReqVO.getVariables();
        variables.put("INITIATOR", userId); // TODO 芋艿：初始化人员
        ProcessInstance instance = runtimeService.startProcessInstanceById(createReqVO.getProcessDefinitionId(), variables);

        // 添加初始的评论 TODO 芋艿：在思考下
        Task task = taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult();
        if (task != null) {
            SysUserDO user = userService.getUser(userId);
            Assert.notNull(user, "用户({})不存在", userId);
            String type = "normal";
            taskService.addComment(task.getId(), instance.getProcessInstanceId(), type,
                    String.format("%s 发起流程申请", user.getNickname()));
            // TODO 芋艿：应该不用下面两个步骤
//           taskService.setAssignee(task.getId(), String.valueOf(userId));
//            taskService.complete(task.getId(), variables);
        }
        return instance.getId();
    }

    public void getMyProcessInstancePage(Long userId) {
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
                .startedBy(String.valueOf(userId)) // 发起人是自己
                .orderByProcessInstanceStartTime().desc(); // 按照发起时间倒序
    }

}
