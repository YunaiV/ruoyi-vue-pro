package cn.iocoder.yudao.adminserver.modules.bpm.service.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelPageReqVo;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelVO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.activiti.engine.repository.Model;

/**
 * 工作流模型接口
 * @author yunlongn
 */
public interface BpmModelService {


    /**
     * 模型数据分页返回
     * @param modelPageReqVo 分页入参
     * @return 分页model
     */
    PageResult<Model> pageList(ModelPageReqVo modelPageReqVo);

    /**
     * 新增一个模型
     * @param modelVO 模型对象
     * @return 返回成功
     */
    CommonResult<String> newModel(ModelVO modelVO);
    /**
     * 修改模型属性，填充bpmn数据
     * @param modelVO 模型对象
     * @return 返回成功
     */
    CommonResult<String> updateModel(ModelVO modelVO);

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
