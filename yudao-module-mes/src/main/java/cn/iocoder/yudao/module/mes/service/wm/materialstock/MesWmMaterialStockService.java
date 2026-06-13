package cn.iocoder.yudao.module.mes.service.wm.materialstock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockFreezeReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockListReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * MES 库存台账 Service 接口
 */
public interface MesWmMaterialStockService {

    /**
     * 获得库存记录
     *
     * @param id 编号
     * @return 库存记录
     */
    MesWmMaterialStockDO getMaterialStock(Long id);

    /**
     * 查询指定库位的库存记录列表
     *
     * @param areaId 库位编号
     * @return 库存记录列表
     */
    List<MesWmMaterialStockDO> getMaterialStockListByAreaId(Long areaId);

    /**
     * 获得库存台账分页
     *
     * @param pageReqVO 分页参数
     * @return 库存台账分页
     */
    PageResult<MesWmMaterialStockDO> getMaterialStockPage(MesWmMaterialStockPageReqVO pageReqVO);

    /**
     * 更新库存冻结状态
     *
     * @param updateReqVO 更新信息
     */
    void updateMaterialStockFrozen(@Valid MesWmMaterialStockFreezeReqVO updateReqVO);

    /**
     * 批量更新库存冻结状态
     *
     * @param ids 库存记录编号列表
     * @param frozen 是否冻结
     */
    void updateMaterialStockFrozen(Collection<Long> ids, boolean frozen);

    /**
     * 获得仓库下库存记录数量
     *
     * @param warehouseId 仓库编号
     * @return 记录数量
     */
    Long getMaterialStockCountByWarehouseId(Long warehouseId);

    /**
     * 获得库区下库存记录数量
     *
     * @param locationId 库区编号
     * @return 记录数量
     */
    Long getMaterialStockCountByLocationId(Long locationId);

    /**
     * 获得库位下库存记录数量
     *
     * @param areaId 库位编号
     * @return 记录数量
     */
    Long getMaterialStockCountByAreaId(Long areaId);

    /**
     * 按编号集合获得库存记录列表
     *
     * @param ids 编号集合
     * @return 库存记录列表
     */
    List<MesWmMaterialStockDO> getMaterialStockList(Collection<Long> ids);

    /**
     * 获得物料库存列表（用于盘点等场景）
     *
     * @param reqVO 查询条件
     * @return 物料库存列表
     */
    List<MesWmMaterialStockDO> getMaterialStockList(MesWmMaterialStockListReqVO reqVO);

    /**
     * 获取或创建库存记录（按组合键唯一）
     *
     * @param itemId         物料编号
     * @param warehouseId    仓库编号
     * @param locationId     库区编号
     * @param areaId         库位编号
     * @param batchId        批次编号
     * @param batchCode      批次号
     * @param vendorId       供应商编号
     * @param receiptTime    入库时间（为空则默认当前时间）
     * @return 库存记录
     */
    MesWmMaterialStockDO getOrCreateMaterialStock(Long itemId, Long warehouseId, Long locationId, Long areaId,
                                                  Long batchId, String batchCode, Long vendorId, LocalDateTime receiptTime);

    /**
     * 更新库存数量
     *
     * @param id 库存记录编号
     * @param quantity        变动数量（正数=增加，负数=扣减）
     * @param checkFlag       是否校验库存充足（为 true 且扣减后为负则报错）
     */
    void updateMaterialStockQuantity(Long id, BigDecimal quantity, boolean checkFlag);

    /**
     * 库位混放规则校验
     *
     * 根据库位配置的物料和批次混放规则，检查是否允许在该库位存放指定物料/批次
     *
     * @param areaId  库位编号
     * @param itemId  物料编号
     * @param batchId 批次编号（可为 null）
     */
    void checkAreaMixingRule(Long areaId, Long itemId, Long batchId);

    /**
     * 校验前端选择的库存记录，兜底避免串单或越权提交
     *
     * @param materialStockId 库存记录编号
     * @param itemId          物料编号
     * @param batchId         批次编号
     * @param batchCode       批次号（可选）
     * @param warehouseId     仓库编号
     * @param locationId      库区编号
     * @param areaId          库位编号
     * @param quantity        数量（可选，校验库存充足）
     * @return 校验通过的库存记录
     */
    MesWmMaterialStockDO validateSelectedStock(Long materialStockId, Long itemId, Long batchId, String batchCode,
                                               Long warehouseId, Long locationId, Long areaId, BigDecimal quantity);

}
