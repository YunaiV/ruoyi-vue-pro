package cn.iocoder.yudao.module.wms.service.stock.ownership;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 所有者库存 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockOwnershipService {

    /**
     * 创建所有者库存
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockOwnershipDO createStockOwnership(@Valid WmsStockOwnershipSaveReqVO createReqVO);

    /**
     * 更新所有者库存
     *
     * @param updateReqVO 更新信息
     */
    WmsStockOwnershipDO updateStockOwnership(@Valid WmsStockOwnershipSaveReqVO updateReqVO);

    /**
     * 删除所有者库存
     *
     * @param id 编号
     */
    void deleteStockOwnership(Long id);

    /**
     * 获得所有者库存
     *
     * @param id 编号
     * @return 所有者库存
     */
    WmsStockOwnershipDO getStockOwnership(Long id);

    /**
     * 获得所有者库存分页
     *
     * @param pageReqVO 分页查询
     * @return 所有者库存分页
     */
    PageResult<WmsStockOwnershipDO> getStockOwnershipPage(WmsStockOwnershipPageReqVO pageReqVO);

    /**
     * 获取库存归属
     *
     * @param warehouseId 仓库id
     * @param productId   产品id
     * @return 库存归属
     */
    List<WmsStockOwnershipDO> selectStockOwnership(Long warehouseId, Long productId);

    List<WmsStockOwnershipDO> selectStockOwnership(Long warehouseId, List<Long> productId);

    WmsStockOwnershipDO getByUkProductOwner(Long warehouseId, Long companyId, Long deptId, Long productId);

    void insertOrUpdate(WmsStockOwnershipDO stockOwnershipDO);

    void assembleProducts(List<WmsStockOwnershipRespVO> list);

    void assembleWarehouse(List<WmsStockOwnershipRespVO> list);

    void assembleDept(List<WmsStockOwnershipRespVO> list);

    void assembleCompany(List<WmsStockOwnershipRespVO> list);

    List<WmsStockOwnershipDO> selectByIds(List<Long> stockOwnershipIds);
}
