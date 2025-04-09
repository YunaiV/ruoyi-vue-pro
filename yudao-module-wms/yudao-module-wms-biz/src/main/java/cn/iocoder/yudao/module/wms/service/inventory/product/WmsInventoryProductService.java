package cn.iocoder.yudao.module.wms.service.inventory.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.product.WmsInventoryProductDO;
import jakarta.validation.Valid;
import java.util.List;

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
    WmsInventoryProductDO createInventoryProduct(@Valid WmsInventoryProductSaveReqVO createReqVO);

    /**
     * 更新库存盘点产品
     *
     * @param updateReqVO 更新信息
     */
    WmsInventoryProductDO updateInventoryProduct(@Valid WmsInventoryProductSaveReqVO updateReqVO);

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

    /**
     * 按 ID 集合查询 WmsInventoryProductDO
     */
    List<WmsInventoryProductDO> selectByIds(List<Long> idList);

    List<WmsInventoryProductDO> selectByInventoryId(Long id);

    void assembleProduct(List<WmsInventoryProductRespVO> inventoryProductList);
}
