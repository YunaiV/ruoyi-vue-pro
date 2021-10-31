package cn.iocoder.yudao.adminserver.modules.activiti.service.oa;

import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.oa.OALeaveDO;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.mysql.oa.OaLeaveMapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class ReportBackEndProcessor implements ExecutionListener {

    @Resource
    private OaLeaveMapper leaveMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(DelegateExecution delegateExecution) {
        final String businessKey = delegateExecution.getProcessInstanceBusinessKey();
        OALeaveDO updateDo = new OALeaveDO();
        updateDo.setId(Long.valueOf(businessKey));
        // TODO @json：status 要枚举起来，不要出现 magic number
        updateDo.setStatus(2);
        leaveMapper.updateById(updateDo);
    }

}
