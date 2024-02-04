package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.ErpWarehouseSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.ErpWarehousePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import jakarta.validation.Valid;

/**
 * ERP 仓库 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpWarehouseService {

    /**
     * 创建仓库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWarehouse(@Valid ErpWarehouseSaveReqVO createReqVO);

    /**
     * 更新ERP 仓库
     *
     * @param updateReqVO 更新信息
     */
    void updateWarehouse(@Valid ErpWarehouseSaveReqVO updateReqVO);

    /**
     * 更新仓库默认状态
     *
     * @param id     编号
     * @param defaultStatus 默认状态
     */
    void updateWarehouseDefaultStatus(Long id, Boolean defaultStatus);

    /**
     * 删除仓库
     *
     * @param id 编号
     */
    void deleteWarehouse(Long id);

    /**
     * 获得仓库
     *
     * @param id 编号
     * @return 仓库
     */
    ErpWarehouseDO getWarehouse(Long id);

    /**
     * 获得仓库分页
     *
     * @param pageReqVO 分页查询
     * @return 仓库分页
     */
    PageResult<ErpWarehouseDO> getWarehousePage(ErpWarehousePageReqVO pageReqVO);

}