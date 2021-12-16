package cn.iocoder.yudao.adminserver.modules.activiti.service.oa;

import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.oa.OaLeaveDO;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.mysql.oa.OaLeaveMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class ReportBackEndProcessor implements ExecutionListener {

    @Resource
    private OaLeaveMapper leaveMapper;


//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void notify(DelegateTask delegateTask) {
//        final String businessKey = delegateTask.getExecution().getProcessInstanceBusinessKey();
//        UpdateWrapper<OaLeaveDO> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.eq("id", Long.valueOf(businessKey));
//        OaLeaveDO updateDo = new OaLeaveDO();
//        updateDo.setStatus(2);
//        leaveMapper.update(updateDo, updateWrapper);
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(DelegateExecution delegateExecution) {
        final String businessKey = delegateExecution.getProcessInstanceBusinessKey();
        // TODO @json：service 不要出现 dao 的元素，例如说 UpdateWrapper。这里，我们可以调用 updateById 方法
        UpdateWrapper<OaLeaveDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", Long.valueOf(businessKey));
        OaLeaveDO updateDo = new OaLeaveDO();
        updateDo.setStatus(2);  // TODO @json：status 要枚举起来，不要出现 magic number
        leaveMapper.update(updateDo, updateWrapper);
    }

}
