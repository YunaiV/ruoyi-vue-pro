package cn.iocoder.yudao.adminserver.modules.bpm.service.model;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.ModelPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 流程模型接口
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
    PageResult<BpmModelRespVO> getModelPage(ModelPageReqVO pageVO);

    // TODO @Li：不用返回 CommonResult
    // TODO @Li：createBpmModal。
    /**
     * 创建流程模型
     *
     * @param modelVO 创建信息
     * @return 创建的流程模型的编号
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
