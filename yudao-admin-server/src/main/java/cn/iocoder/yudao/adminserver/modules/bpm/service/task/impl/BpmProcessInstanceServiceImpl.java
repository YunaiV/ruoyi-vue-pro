package cn.iocoder.yudao.adminserver.modules.bpm.service.task.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstanceCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstanceMyPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstancePageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.task.BpmProcessInstanceConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.task.BpmProcessInstanceExtDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.task.BpmProcessInstanceExtMapper;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.task.BpmProcessInstanceResultEnum;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.adminserver.modules.system.service.user.SysUserService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

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
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;

    @Resource
    private SysUserService userService;
    @Resource
    @Lazy // 解决循环依赖
    private BpmTaskService taskService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Resource
    private BpmProcessInstanceExtMapper processInstanceExtMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createProcessInstance(Long userId, BpmProcessInstanceCreateReqVO createReqVO) {
        // 校验流程定义
        ProcessDefinition definition = processDefinitionService.getProcessDefinition(createReqVO.getProcessDefinitionId());
        if (definition == null) {
            throw exception(PROCESS_DEFINITION_NOT_EXISTS);
        }
        if (definition.isSuspended()) {
            throw exception(PROCESS_DEFINITION_IS_SUSPENDED);
        }

        // 创建流程实例
        Map<String, Object> variables = createReqVO.getVariables();
        ProcessInstance instance = runtimeService.startProcessInstanceById(createReqVO.getProcessDefinitionId(), variables);
        // 设置流程名字
        runtimeService.setProcessInstanceName(instance.getId(), definition.getName());
        // 记录流程实例的拓展表
        createProcessInstanceExt(instance, definition);

        // TODO 芋艿：临时使用, 保证分配
        List<Task> tasks = taskService.getTasksByProcessInstanceId(instance.getId());
        tasks.forEach(task -> taskService.updateTaskAssign(task.getId(), userId));

        // 添加初始的评论 TODO 芋艿：在思考下
//        Task task = taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult();
//        if (task != null) {
//            SysUserDO user = userService.getUser(userId);
//            Assert.notNull(user, "用户({})不存在", userId);
//            String type = "normal";
//            taskService.addComment(task.getId(), instance.getProcessInstanceId(), type,
//                    String.format("%s 发起流程申请", user.getNickname()));
//            // TODO 芋艿：应该不用下面两个步骤
////           taskService.setAssignee(task.getId(), String.valueOf(userId));
////            taskService.complete(task.getId(), variables);
//        }
        return instance.getId();
    }

    /**
     * 创建流程实例的拓展
     *
     * @param definition 流程定义
     * @param instance 流程实例
     */
    private void createProcessInstanceExt(ProcessInstance instance, ProcessDefinition definition) {
        BpmProcessInstanceExtDO instanceExt = BpmProcessInstanceConvert.INSTANCE.convert(instance, definition);
        instanceExt.setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        instanceExt.setResult(BpmProcessInstanceResultEnum.PROCESS.getResult());
        processInstanceExtMapper.insert(instanceExt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProcessInstance(Long userId, BpmProcessInstanceCancelReqVO cancelReqVO) {
        // 校验流程实例存在
        ProcessInstance instance = getProcessInstance(cancelReqVO.getId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS);
        }
        // TODO 芋艿：只能自己取消流程

        // 通过删除流程实例，实现流程实例的取消
        runtimeService.deleteProcessInstance(cancelReqVO.getId(), cancelReqVO.getReason());
        // 更新流程实例的拓展表为取消状态
        updateProcessInstanceResult(cancelReqVO.getId(), BpmProcessInstanceResultEnum.CANCEL.getResult());
    }

    @Override
    public void deleteProcessInstance(String id, String reason) {
        runtimeService.deleteProcessInstance(id, reason);
    }

    @Override
    public void updateProcessInstanceResult(String id, Integer result) {
        processInstanceExtMapper.updateByProcessInstanceId(id, new BpmProcessInstanceExtDO()
                .setStatus(BpmProcessInstanceStatusEnum.FINISH.getStatus()).setResult(result));
    }

    @Override
    public PageResult<BpmProcessInstancePageItemRespVO> getMyProcessInstancePage(Long userId,
                                                                                 BpmProcessInstanceMyPageReqVO pageReqVO) {
        // 通过 BpmProcessInstanceExtDO 表，先查询到对应的分页
        PageResult<BpmProcessInstanceExtDO> pageResult = processInstanceExtMapper.selectPage(userId, pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return new PageResult<>(pageResult.getTotal());
        }

        // 获得流程 Task Map
        List<String> processInstanceIds = convertList(pageResult.getList(), BpmProcessInstanceExtDO::getProcessInstanceId);
        Map<String, List<Task>> taskMap = taskService.getTaskMapByProcessInstanceIds(processInstanceIds);
        // 转换返回
        return BpmProcessInstanceConvert.INSTANCE.convertPage(pageResult, taskMap);
    }

    @Override
    public List<ProcessInstance> getProcessInstances(Set<String> ids) {
        return runtimeService.createProcessInstanceQuery().processInstanceIds(ids).list();
    }

    @Override
    public ProcessInstance getProcessInstance(String id) {
        return runtimeService.createProcessInstanceQuery().processInstanceId(id).singleResult();
    }

    /**
     * 获得历史的流程实例
     *
     * @param id 流程实例的编号
     * @return 历史的流程实例
     */
    private HistoricProcessInstance getHistoricProcessInstance(String id) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(id).singleResult();
    }

}
