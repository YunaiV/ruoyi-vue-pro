package cn.iocoder.yudao.adminserver.modules.bpm.service.model;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.*;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.validation.Valid;

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
    PageResult<BpmModelPageItemRespVO> getModelPage(ModelPageReqVO pageVO);

    /**
     * 获得流程模块
     *
     * @param id 编号
     * @return 流程模型
     */
    BpmModelRespVO getModel(String id);

    /**
     * 创建流程模型
     *
     * @param modelVO 创建信息
     * @return 创建的流程模型的编号
     */
    String createModel(@Valid BpmModelCreateReqVO modelVO);

    /**
     * 修改流程模型
     *
     * @param updateReqVO 更新信息
     */
    void updateModel(@Valid BpmModelUpdateReqVO updateReqVO);

    /**
     * 部署模型 使模型成为一个 process
     * @param modelId 模型Id
     * @return 返回成功
     */
    CommonResult<String> deploy(String modelId);

    /**
     * 删除模型
     *
     * @param id 编号
     */
    void deleteModel(String id);

}
