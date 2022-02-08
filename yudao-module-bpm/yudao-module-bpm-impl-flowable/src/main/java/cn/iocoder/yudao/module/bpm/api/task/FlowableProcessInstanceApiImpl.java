package cn.iocoder.yudao.module.bpm.api.task;

import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
/**
 * Flowable 流程实例 Api 实现类
 *
 * @author 芋道源码
 * @author jason
 */
@Service
@Validated
public class FlowableProcessInstanceApiImpl implements BpmProcessInstanceApi {

    @Override
    public String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqDTO reqDTO) {
        //TODO
        return null;
    }
}
