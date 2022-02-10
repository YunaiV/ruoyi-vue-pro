package cn.iocoder.yudao.module.bpm.service.definition;

import org.flowable.bpmn.model.BpmnModel;

/**
 * Flowable流程模型接口
 *
 * @author yunlongn
 */
public interface BpmModelService extends BpmModelCommonService {

    /**
     * 获得流程模型编号对应的 BPMN Model
     *
     * @param id 流程模型编号
     * @return BPMN Model
     */
    BpmnModel getBpmnModel(String id);

}
