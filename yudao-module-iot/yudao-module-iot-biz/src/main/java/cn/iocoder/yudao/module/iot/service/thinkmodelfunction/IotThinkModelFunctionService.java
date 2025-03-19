package cn.iocoder.yudao.module.iot.service.thinkmodelfunction;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionPageReqVO;
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
     * 创建产品物模型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createThinkModelFunction(@Valid IotThinkModelFunctionSaveReqVO createReqVO);


    /**
     * 更新产品物模型
     *
     * @param updateReqVO 更新信息
     */
    void updateThinkModelFunction(@Valid IotThinkModelFunctionSaveReqVO updateReqVO);

    /**
     * 删除产品物模型
     *
     * @param id 编号
     */
    void deleteThinkModelFunction(Long id);

    /**
     * 获得产品物模型
     *
     * @param id 编号
     * @return 产品物模型
     */
    IotThinkModelFunctionDO getThinkModelFunction(Long id);

    /**
     * 获得产品物模型列表
     *
     * @param productId 产品编号
     * @return 产品物模型列表
     */
    List<IotThinkModelFunctionDO> getThinkModelFunctionListByProductId(Long productId);

    /**
     * 获得产品物模型分页
     *
     * @param pageReqVO 分页查询
     * @return 产品物模型分页
     */
    PageResult<IotThinkModelFunctionDO> getThinkModelFunctionPage(IotThinkModelFunctionPageReqVO pageReqVO);

}