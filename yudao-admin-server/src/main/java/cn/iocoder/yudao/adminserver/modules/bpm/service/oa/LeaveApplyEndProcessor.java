package cn.iocoder.yudao.adminserver.modules.bpm.service.oa;

import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.leave.OALeaveDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.oa.OALeaveMapper;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.task.BpmProcessInstanceResultEnum;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;


/**
 * 请假流程结束流程监听处理服务， 根据请假申请审批通过，还是未通过， 更新请假表单
 */
@Component
public class LeaveApplyEndProcessor implements ExecutionListener {

    @Resource
    private OALeaveMapper leaveMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(DelegateExecution delegateExecution) {
        final String businessKey = delegateExecution.getProcessInstanceBusinessKey();
        final Object approved = delegateExecution.getVariable("approved");
        OALeaveDO updateDo = new OALeaveDO();
        updateDo.setId(Long.valueOf(businessKey));
        if (Objects.equals(approved, true)) {
            updateDo.setStatus(BpmProcessInstanceResultEnum.APPROVE.getResult());
        } else {
            updateDo.setStatus(BpmProcessInstanceResultEnum.REJECT.getResult());
        }

        leaveMapper.updateById(updateDo);
    }

}
