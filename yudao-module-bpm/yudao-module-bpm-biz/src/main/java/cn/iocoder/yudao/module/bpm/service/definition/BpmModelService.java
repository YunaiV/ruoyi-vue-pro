package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelUpdateReqVO;
import jakarta.validation.Valid;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.repository.Model;

/**
 * Flowable流程模型接口
 *
 * @author yunlongn
 */
public interface BpmModelService {

    /**
     * 获得流程模型分页
     *
     * @param pageVO 分页查询
     * @return 流程模型分页
     */
    PageResult<Model> getModelPage(BpmModelPageReqVO pageVO);

    /**
     * 创建流程模型
     *
     * @param modelVO 创建信息
     * @return 创建的流程模型的编号
     */
    String createModel(@Valid BpmModelSaveReqVO modelVO);

    /**
     * 获得流程模块
     *
     * @param id 编号
     * @return 流程模型
     */
    Model getModel(String id);

    /**
     * 获得流程模型的 BPMN XML
     *
     * @param id 编号
     * @return BPMN XML
     */
    byte[] getModelBpmnXML(String id);

    /**
     * 修改流程模型的 BPMN XML
     *
     * @param id      编号
     * @param bpmnXml BPMN XML
     */
    void updateModelBpmnXml(String id, String bpmnXml);

    /**
     * 修改流程模型
     *
     * @param userId 用户编号
     * @param updateReqVO 更新信息
     */
    void updateModel(Long userId, @Valid BpmModelSaveReqVO updateReqVO);

    /**
     * 将流程模型，部署成一个流程定义
     *
     * @param userId 用户编号
     * @param id 编号
     */
    void deployModel(Long userId, String id);

    /**
     * 删除模型
     *
     * @param userId  用户编号
     * @param id 编号
     */
    void deleteModel(Long userId, String id);

    /**
     * 修改模型的状态，实际更新的部署的流程定义的状态
     *
     * @param userId 用户编号
     * @param id    编号
     * @param state 状态
     */
    void updateModelState(Long userId, String id, Integer state);

    /**
     * 获得流程定义编号对应的 BPMN Model
     *
     * @param processDefinitionId 流程定义编号
     * @return BPMN Model
     */
    BpmnModel getBpmnModelByDefinitionId(String processDefinitionId);

    // ========== 仿钉钉/飞书的精简模型 =========

    /**
     * 获取仿钉钉流程设计模型结构
     *
     * @param modelId 流程模型编号
     * @return 仿钉钉流程设计模型结构
     */
    BpmSimpleModelNodeVO getSimpleModel(String modelId);

    /**
     * 更新仿钉钉流程设计模型
     *
     * @param userId 用户编号
     * @param reqVO 请求信息
     */
    void updateSimpleModel(Long userId, @Valid BpmSimpleModelUpdateReqVO reqVO);

}
