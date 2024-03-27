package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.simple.BpmSimpleModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.repository.Model;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.MODEL_NOT_EXISTS;

/**
 * 仿钉钉流程设计 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class BpmSimpleModelServiceImpl implements BpmSimpleModelService {
    @Resource
    private BpmModelService bpmModelService;

    @Override
    public Boolean saveSimpleModel(BpmSimpleModelSaveReqVO reqVO) {
        Model model = bpmModelService.getModel(reqVO.getModelId());
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        byte[] bpmnBytes = bpmModelService.getModelBpmnXML(reqVO.getModelId());

        if (ArrayUtil.isEmpty(bpmnBytes)) {
            //  BPMN XML 不存在。新增
            BpmnModel bpmnModel = BpmnModelUtils.convertSimpleModelToBpmnModel(model.getKey(), model.getName(), reqVO.getSimpleModelBody());
            bpmModelService.saveModelBpmnXml(model.getId(),BpmnModelUtils.getBpmnXml(bpmnModel));
            return Boolean.TRUE;
        } else {
            // TODO BPMN XML 已经存在。如何修改 ??
            return Boolean.FALSE;
        }
    }
}
