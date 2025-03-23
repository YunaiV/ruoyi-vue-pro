package cn.iocoder.yudao.module.iot.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.script.IotProductScriptPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.script.IotProductScriptSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.script.IotProductScriptTestReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.script.IotProductScriptTestRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductScriptDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 产品脚本信息 Service 接口
 *
 * @author 芋道源码
 */
public interface IotProductScriptService {

    /**
     * 创建IoT 产品脚本信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductScript(@Valid IotProductScriptSaveReqVO createReqVO);

    /**
     * 更新IoT 产品脚本信息
     *
     * @param updateReqVO 更新信息
     */
    void updateProductScript(@Valid IotProductScriptSaveReqVO updateReqVO);

    /**
     * 删除IoT 产品脚本信息
     *
     * @param id 编号
     */
    void deleteProductScript(Long id);

    /**
     * 获得IoT 产品脚本信息
     *
     * @param id 编号
     * @return IoT 产品脚本信息
     */
    IotProductScriptDO getProductScript(Long id);

    /**
     * 获得IoT 产品脚本信息分页
     *
     * @param pageReqVO 分页查询
     * @return IoT 产品脚本信息分页
     */
    PageResult<IotProductScriptDO> getProductScriptPage(IotProductScriptPageReqVO pageReqVO);

    /**
     * 获取产品的脚本列表
     *
     * @param productId 产品ID
     * @return 脚本列表
     */
    List<IotProductScriptDO> getProductScriptListByProductId(Long productId);

    /**
     * 测试产品脚本
     *
     * @param testReqVO 测试请求
     * @return 测试结果
     */
    IotProductScriptTestRespVO testProductScript(@Valid IotProductScriptTestReqVO testReqVO);

    /**
     * 更新产品脚本状态
     *
     * @param id     脚本ID
     * @param status 状态
     */
    void updateProductScriptStatus(Long id, Integer status);

}