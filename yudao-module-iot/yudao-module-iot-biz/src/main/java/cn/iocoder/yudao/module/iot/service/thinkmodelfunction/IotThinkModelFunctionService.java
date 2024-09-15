package cn.iocoder.yudao.module.iot.service.thinkmodelfunction;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 产品物模型 Service 接口
 *
 * @author 芋道源码
 */
public interface IotThinkModelFunctionService {

    /**
     * 创建IoT 产品物模型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createThinkModelFunction(@Valid IotThinkModelFunctionSaveReqVO createReqVO);


    /**
     * 更新IoT 产品物模型
     *
     * @param updateReqVO 更新信息
     */
    void updateThinkModelFunction(@Valid IotThinkModelFunctionSaveReqVO updateReqVO);

    /**
     * 删除IoT 产品物模型
     *
     * @param id 编号
     */
    void deleteThinkModelFunction(Long id);

    /**
     * 获得IoT 产品物模型
     *
     * @param id 编号
     * @return IoT 产品物模型
     */
    IotThinkModelFunctionDO getThinkModelFunction(Long id);

    /**
     * 获得IoT 产品物模型列表
     *
     * @param productId 产品编号
     * @return IoT 产品物模型列表
     */
    List<IotThinkModelFunctionDO> getThinkModelFunctionListByProductId(Long productId);
}