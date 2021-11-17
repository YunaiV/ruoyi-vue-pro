package cn.iocoder.yudao.adminserver.modules.bpm.service.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelCreateVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelUpdateVO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;

/**
 * 工作流模型接口
 * @author yunlongn
 */
public interface BpmModelService {

    /**
     * 新增一个模型
     * @param modelCreateVO 模型对象
     * @return 返回成功
     */
    CommonResult<String> newModel(ModelCreateVO modelCreateVO);

    CommonResult<String> updateModel(ModelUpdateVO modelUpdateVO);

    CommonResult<String> deploy(String modelId);


}
