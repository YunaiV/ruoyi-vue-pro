package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.simple.BpmSimpleModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.repository.Model;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.MODEL_NOT_EXISTS;

// TODO @jason：这块可以讨论下，是不是合并成一个 BpmnModelServiceImpl
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
        // 1.1 校验流程模型存在
        Model model = bpmModelService.getModel(reqVO.getModelId());
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }

        // 1. JSON 转换成 bpmnModel
        BpmnModel bpmnModel = SimpleModelUtils.convertSimpleModelToBpmnModel(model.getKey(), model.getName(), reqVO.getSimpleModelBody());
        // 2.1 保存 Bpmn XML
        bpmModelService.saveModelBpmnXml(model.getId(), StrUtil.utf8Bytes(BpmnModelUtils.getBpmnXml(bpmnModel)));
        // 2.2 保存 JSON 数据
        bpmModelService.saveModelSimpleJson(model.getId(), JsonUtils.toJsonByte(reqVO.getSimpleModelBody()));
        return Boolean.TRUE;
    }

    @Override
    public BpmSimpleModelNodeVO getSimpleModel(String modelId) {
        Model model = bpmModelService.getModel(modelId);
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        // 暂时不用 bpmn 转 json， 有点复杂,
        // 通过 ACT_RE_MODEL 表 EDITOR_SOURCE_EXTRA_VALUE_ID_  获取 仿钉钉快搭模型的JSON 数据
        byte[] jsonBytes = bpmModelService.getModelSimpleJson(model.getId());
        return JsonUtils.parseObject(jsonBytes, BpmSimpleModelNodeVO.class);
    }

}
