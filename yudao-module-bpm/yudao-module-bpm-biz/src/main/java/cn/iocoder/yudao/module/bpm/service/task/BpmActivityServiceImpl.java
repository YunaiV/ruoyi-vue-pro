package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.activity.BpmActivityRespVO;
import cn.iocoder.yudao.module.bpm.convert.task.BpmActivityConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmActivityDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.task.BpmActivityMapper;
import lombok.extern.slf4j.Slf4j;

import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;


/**
 * BPM 活动实例 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
@Validated
public class BpmActivityServiceImpl implements BpmActivityService {

    @Resource
    private HistoryService historyService;
    @Resource
    private BpmActivityMapper bpmActivityMapper;

    @Override
    @TenantIgnore
    public List<BpmActivityRespVO> getActivityListByProcessInstanceId(String procInstId) {
//        List<HistoricActivityInstance> activityList = historyService.createHistoricActivityInstanceQuery()
//                .processInstanceId(procInstId).list();
        // TODO @ke：这个是想要过滤掉 aha.act_type_ != 'sequenceFlow' 类型么？
        List<BpmActivityDO> bpmActivityDOList = bpmActivityMapper.listAllByProcInstIdAndDelete(procInstId);
        return BpmActivityConvert.INSTANCE.convertList(bpmActivityDOList);
    }

    @Override
    public HistoricActivityInstance getHistoricActivityByExecutionId(String executionId) {
        return historyService.createHistoricActivityInstanceQuery().executionId(executionId).singleResult();
    }

}
