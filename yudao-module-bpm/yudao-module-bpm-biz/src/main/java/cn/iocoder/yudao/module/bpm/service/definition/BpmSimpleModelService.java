package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.simple.BpmSimpleModelSaveReqVO;
import jakarta.validation.Valid;

/**
 * 仿钉钉流程设计 Service 接口
 *
 * @author jason
 */
public interface BpmSimpleModelService {

    /**
     * 保存仿钉钉流程设计模型
     * @param reqVO 请求信息
     */
    Boolean saveSimpleModel(@Valid  BpmSimpleModelSaveReqVO reqVO);

    /**
     * 获取仿钉钉流程设计模型结构
     * @param modelId 流程模型编号
     * @return 仿钉钉流程设计模型结构
     */
    BpmSimpleModelNodeVO getSimpleModel(String modelId);
}
