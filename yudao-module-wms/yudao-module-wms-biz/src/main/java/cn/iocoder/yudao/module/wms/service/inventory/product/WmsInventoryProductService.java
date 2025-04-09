package cn.iocoder.yudao.module.wms.service.inventory.product;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.product.WmsInventoryProductDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 库存盘点产品 Service 接口
 *
 * @author 李方捷
 */
public interface WmsInventoryProductService {

    /**
     * 创建库存盘点产品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInventoryProduct(@Valid WmsInventoryProductSaveReqVO createReqVO);

    /**
     * 更新库存盘点产品
     *
     * @param updateReqVO 更新信息
     */
    void updateInventoryProduct(@Valid WmsInventoryProductSaveReqVO updateReqVO);

    /**
     * 删除库存盘点产品
     *
     * @param id 编号
     */
    void deleteInventoryProduct(Long id);

    /**
     * 获得库存盘点产品
     *
     * @param id 编号
     * @return 库存盘点产品
     */
    WmsInventoryProductDO getInventoryProduct(Long id);

    /**
     * 获得库存盘点产品分页
     *
     * @param pageReqVO 分页查询
     * @return 库存盘点产品分页
     */
    PageResult<WmsInventoryProductDO> getInventoryProductPage(WmsInventoryProductPageReqVO pageReqVO);

}