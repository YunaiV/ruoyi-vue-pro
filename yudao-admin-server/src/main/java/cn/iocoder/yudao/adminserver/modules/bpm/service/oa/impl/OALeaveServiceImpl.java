package cn.iocoder.yudao.adminserver.modules.bpm.service.oa.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo.*;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.oa.OALeaveConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.leave.OALeaveDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.oa.OALeaveMapper;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.FlowStatusEnum;
import cn.iocoder.yudao.adminserver.modules.bpm.service.oa.OALeaveService;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.user.SysUserBaseVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysPostDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.dept.SysPostMapper;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.user.SysUserMapper;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;

import static cn.iocoder.yudao.adminserver.modules.bpm.enums.oa.OAErrorCodeConstants.*;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.POST_CODE_DUPLICATE;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 请假申请 Service 实现类
 *
 * @author 芋艿
 */
@Service
@Validated
public class OALeaveServiceImpl implements OALeaveService {

    @Resource
    private OALeaveMapper leaveMapper;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private org.activiti.engine.TaskService activitiTaskService;

    @Resource
    private TaskRuntime taskRuntime;

    @Resource
    private SysPostMapper sysPostMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLeave(OALeaveCreateReqVO createReqVO) {
        // 插入 OA 请假单
        OALeaveDO leave = OALeaveConvert.INSTANCE.convert(createReqVO);
        leave.setStatus(FlowStatusEnum.HANDLE.getStatus());
        // TODO @jason：应该是存储 userId？？
        leave.setUserId(SecurityFrameworkUtils.getLoginUser().getUsername());
        leaveMapper.insert(leave);
        Date startTime = createReqVO.getStartTime();
        Date endTime = createReqVO.getEndTime();
        long day = DateUtil.betweenDay(startTime, endTime, false);
        if (day <= 0) {
            throw ServiceExceptionUtil.exception(DAY_LEAVE_ERROR);
        }
        Map<String, Object> taskVariables = createReqVO.getTaskVariables();
        taskVariables.put("day", day);
        // 创建工作流
        Long id = leave.getId();
        String businessKey = String.valueOf(id);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(createReqVO.getProcessKey(), businessKey, taskVariables);
        String processInstanceId = processInstance.getProcessInstanceId();

        // 将工作流的编号，更新到 OA 请假单中
        OALeaveDO updateDo = new OALeaveDO();
        updateDo.setProcessInstanceId(processInstanceId);
        updateDo.setId(id);
        leaveMapper.updateById(updateDo);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLeave(OALeaveUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateLeaveExists(updateReqVO.getId());

        final Task task = taskRuntime.task(updateReqVO.getTaskId());
        activitiTaskService.addComment(task.getId(), task.getProcessInstanceId(), updateReqVO.getComment());
        Map<String, Object> variables = updateReqVO.getVariables();

        //如何得到部门领导人， 暂时写死
        variables.put("deptLeader", "admin");
        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId())
                .withVariables(variables)
                .build());

        // TOTO @芋道源码 貌似 IDEA 会自动加上final（不需要加 final 哈。虽然是不变，但是代码比较少这么去写)
        Object reApply = variables.get("reApply");
        if(Objects.equals(reApply, true)){
            // 更新 表单
            OALeaveDO updateObj = OALeaveConvert.INSTANCE.convert(updateReqVO);
            leaveMapper.updateById(updateObj);
        }
    }

    @Override
    public void deleteLeave(Long id) {
        // 校验存在
        this.validateLeaveExists(id);
        // 删除
        leaveMapper.deleteById(id);
    }

    private void validateLeaveExists(Long id) {
        if (leaveMapper.selectById(id) == null) {
            throw exception(LEAVE_NOT_EXISTS);
        }
    }

    @Override
    public OALeaveDO getLeave(Long id) {
        return leaveMapper.selectById(id);
    }

    @Override
    public List<OALeaveDO> getLeaveList(Collection<Long> ids) {
        return leaveMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<OALeaveDO> getLeavePage(OALeavePageReqVO pageReqVO) {
        return leaveMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OALeaveDO> getLeaveList(OALeaveExportReqVO exportReqVO) {
        return leaveMapper.selectList(exportReqVO);
    }


    /**
     * 获取本人请假申请流程中审批人员
     * @return 包括，本人部门的项目经理， 部门经理，  hr
     */
    @Override
    public OALeaveApplyMembersVO getLeaveApplyMembers() {
        final Long id = SecurityFrameworkUtils.getLoginUser().getId();
        //项目经理
        //TODO jason 定义enum
        SysPostDO pmPostDO = sysPostMapper.selectByCode("pm");
        if (Objects.isNull(pmPostDO)) {
            throw ServiceExceptionUtil.exception(PM_POST_NOT_EXISTS);
        }
        SysUserDO userDO = sysUserMapper.selectById(id);
        Set<Long>  postIds  = new HashSet<>(8);
        postIds.add( pmPostDO.getId());
        SysUserBaseVO baseVO = new SysUserBaseVO();
        baseVO.setDeptId(userDO.getDeptId())
                .setPostIds(postIds);
        final List<SysUserDO> pmUsers = sysUserMapper.selectListByBaseVO(baseVO);
        if (CollUtil.isEmpty(pmUsers)) {
            throw ServiceExceptionUtil.exception(DEPART_PM_POST_NOT_EXISTS);
        }

        //部门经理
        SysPostDO bmPostDO = sysPostMapper.selectByCode("bm");
        if (Objects.isNull(bmPostDO)) {
            throw ServiceExceptionUtil.exception(BM_POST_NOT_EXISTS);
        }
        userDO = sysUserMapper.selectById(id);
        postIds  = new HashSet<>(8);
        postIds.add( bmPostDO.getId());
        baseVO = new SysUserBaseVO();
        baseVO.setDeptId(userDO.getDeptId())
                .setPostIds(postIds);
        final List<SysUserDO> bmUsers = sysUserMapper.selectListByBaseVO(baseVO);
        if (CollUtil.isEmpty(bmUsers)) {
            throw ServiceExceptionUtil.exception(DEPART_BM_POST_NOT_EXISTS);
        }
        //人事
        SysPostDO hrPostDO = sysPostMapper.selectByCode("hr");
        if (Objects.isNull(hrPostDO)) {
            throw ServiceExceptionUtil.exception(HR_POST_NOT_EXISTS);
        }
        userDO = sysUserMapper.selectById(id);
        postIds  = new HashSet<>(8);
        postIds.add( hrPostDO.getId());
        baseVO = new SysUserBaseVO();
        baseVO.setDeptId(userDO.getDeptId())
                .setPostIds(postIds);
        final List<SysUserDO> hrUsers = sysUserMapper.selectListByBaseVO(baseVO);
        if (CollUtil.isEmpty(hrUsers)) {
            throw ServiceExceptionUtil.exception(DEPART_BM_POST_NOT_EXISTS);
        }
        return OALeaveApplyMembersVO.builder().pm(pmUsers.get(0).getUsername())
                .bm(bmUsers.get(0).getUsername())
                .hr(hrUsers.get(0).getUsername()).build();
    }

}
