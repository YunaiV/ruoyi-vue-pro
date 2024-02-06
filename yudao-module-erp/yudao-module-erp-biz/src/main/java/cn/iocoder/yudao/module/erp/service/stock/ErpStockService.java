package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.stock.ErpStockPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;

/**
 * ERP 产品库存 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpStockService {

    /**
     * 获得产品库存
     *
     * @param id 编号
     * @return 库存
     */
    ErpStockDO getStock(Long id);

    /**
     * 基于产品 + 仓库，获得产品库存
     *
     * @param productId 产品编号
     * @param warehouseId 仓库编号
     * @return 产品库存
     */
    ErpStockDO getStock(Long productId, Long warehouseId);

    /**
     * 获得产品库存分页
     *
     * @param pageReqVO 分页查询
     * @return 库存分页
     */
    PageResult<ErpStockDO> getStockPage(ErpStockPageReqVO pageReqVO);

}