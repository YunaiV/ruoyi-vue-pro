package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.*;
import org.activiti.bpmn.model.BpmnModel;

import javax.validation.Valid;

/**
 * 流程模型接口
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
