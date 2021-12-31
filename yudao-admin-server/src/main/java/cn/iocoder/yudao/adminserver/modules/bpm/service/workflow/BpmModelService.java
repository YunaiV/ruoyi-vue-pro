package cn.iocoder.yudao.adminserver.modules.bpm.service.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.ModelPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelCreateReqVO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.activiti.engine.repository.Model;

/**
 * 流程定义接口
 *
 * @author yunlongn
 */
public interface BpmModelService {

    /**
     * 获得流程定义分页
     *
     * @param pageVO 分页查询
     * @return 流程定义分页
     */
    PageResult<Model> getModelPage(ModelPageReqVO pageVO);

    // TODO @Li：不用返回 CommonResult
    // TODO @Li：createBpmModal。
    /**
     * 创建流程定义
     *
     * @param modelVO 创建信息
     * @return 创建的流程定义的编号
     */
    String createModel(BpmModelCreateReqVO modelVO);

    /**
     * 修改模型属性，填充bpmn数据
     * @param modelVO 模型对象
     * @return 返回成功
     */
    CommonResult<String> updateModel(BpmModelCreateReqVO modelVO);

    /**
     * 部署模型 使模型成为一个 process
     * @param modelId 模型Id
     * @return 返回成功
     */
    CommonResult<String> deploy(String modelId);

    /**
     * 导出模型
     * @param modelId 模型Id
     * @return {@link FileResp} 返回文件
     */
    FileResp exportBpmnXml(String modelId);

    /**
     * 删除模型
     * @param modelId 模型Id
     * @return 返回成功
     */
    CommonResult<String> deleteModel(String modelId);
}
