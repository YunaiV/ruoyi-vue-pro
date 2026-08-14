package cn.iocoder.yudao.module.bpm.api.task;

import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Flowable 流程实例 Api 实现类
 *
 * @author 芋道源码
 * @author jason
 */
@Service
@Validated
public class BpmProcessInstanceApiImpl implements BpmProcessInstanceApi {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqDTO reqDTO) {
        return processInstanceService.createProcessInstance(userId, reqDTO);
    }

    @Override
    public void cancelProcessInstanceByStartUser(Long userId, String processInstanceId, String reason) {
        processInstanceService.cancelProcessInstanceByStartUser(userId,
                new BpmProcessInstanceCancelReqVO().setId(processInstanceId).setReason(reason));
    }

}
