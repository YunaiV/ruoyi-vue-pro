package cn.iocoder.yudao.module.mes.dal.mysql.wm.materialstock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockListReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * MES 库存台账 Mapper
 */
@Mapper
public interface MesWmMaterialStockMapper extends BaseMapperX<MesWmMaterialStockDO> {

    default PageResult<MesWmMaterialStockDO> selectPage(MesWmMaterialStockPageReqVO reqVO,
                                                         Collection<Long> itemTypeIds,
                                                         Collection<Long> itemIds,
                                                         Long virtualWarehouseId) {
        LambdaQueryWrapperX<MesWmMaterialStockDO> wrapper = new LambdaQueryWrapperX<MesWmMaterialStockDO>()
                .inIfPresent(MesWmMaterialStockDO::getItemTypeId, itemTypeIds)
                .inIfPresent(MesWmMaterialStockDO::getItemId, itemIds)
                .likeIfPresent(MesWmMaterialStockDO::getBatchCode, reqVO.getBatchCode())
                .eqIfPresent(MesWmMaterialStockDO::getBatchId, reqVO.getBatchId())
                .eqIfPresent(MesWmMaterialStockDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(MesWmMaterialStockDO::getLocationId, reqVO.getLocationId())
                .eqIfPresent(MesWmMaterialStockDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(MesWmMaterialStockDO::getVendorId, reqVO.getVendorId())
                .eqIfPresent(MesWmMaterialStockDO::getFrozen, reqVO.getFrozen());
        wrapper.ne(MesWmMaterialStockDO::getQuantity, BigDecimal.ZERO)
                .orderByAsc(MesWmMaterialStockDO::getReceiptTime);
        // 虚拟仓过滤（Service 层已将 virtualFilter 解析为 virtualWarehouseId）
        if (virtualWarehouseId != null) {
            if (MesWmMaterialStockPageReqVO.VIRTUAL_FILTER_ONLY.equals(reqVO.getVirtualFilter())) {
                wrapper.eq(MesWmMaterialStockDO::getWarehouseId, virtualWarehouseId);
            } else if (MesWmMaterialStockPageReqVO.VIRTUAL_FILTER_EXCLUDE.equals(reqVO.getVirtualFilter())) {
                wrapper.ne(MesWmMaterialStockDO::getWarehouseId, virtualWarehouseId);
            }
        }
        return selectPage(reqVO, wrapper);
    }

    default Long selectCountByWarehouseId(Long warehouseId) {
        return selectCount(MesWmMaterialStockDO::getWarehouseId, warehouseId);
    }

    default Long selectCountByLocationId(Long locationId) {
        return selectCount(MesWmMaterialStockDO::getLocationId, locationId);
    }

    default Long selectCountByAreaId(Long areaId) {
        return selectCount(MesWmMaterialStockDO::getAreaId, areaId);
    }

    default List<MesWmMaterialStockDO> selectListByIds(Collection<Long> ids) {
        return selectByIds(ids);
    }

    default void updateByIds(Collection<Long> ids, MesWmMaterialStockDO updateObj) {
        update(updateObj, new LambdaUpdateWrapper<MesWmMaterialStockDO>()
                .in(MesWmMaterialStockDO::getId, ids));
    }

    /**
     * 增量更新库存数量
     *
     * @param id        库存记录编号
     * @param count     变动数量（正数=增加，负数=扣减）
     * @param checkFlag 是否校验库存充足（为 true 时扣减不允许变为负数）
     * @return 影响行数
     */
    default int updateQuantity(Long id, BigDecimal count, boolean checkFlag) {
        LambdaUpdateWrapper<MesWmMaterialStockDO> updateWrapper = new LambdaUpdateWrapper<MesWmMaterialStockDO>()
                .eq(MesWmMaterialStockDO::getId, id)
                .setSql("quantity = quantity + " + count);
        if (checkFlag && count.compareTo(BigDecimal.ZERO) < 0) {
            updateWrapper.ge(MesWmMaterialStockDO::getQuantity, count.abs()); // CAS 防负库存
        }
        return update(null, updateWrapper);
    }

    default MesWmMaterialStockDO selectByCompositeKey(Long itemId, Long warehouseId, Long locationId,
                                                       Long areaId, Long batchId) {
        LambdaQueryWrapperX<MesWmMaterialStockDO> wrapper = new LambdaQueryWrapperX<MesWmMaterialStockDO>()
                .eqIfPresent(MesWmMaterialStockDO::getItemId, itemId)
                .eqIfPresent(MesWmMaterialStockDO::getWarehouseId, warehouseId)
                .eqIfPresent(MesWmMaterialStockDO::getLocationId, locationId)
                .eqIfPresent(MesWmMaterialStockDO::getAreaId, areaId);
        // batchId=null 时精确匹配 is null，避免 eqIfPresent 跳过条件导致匹配到其他批次
        if (batchId != null) {
            wrapper.eq(MesWmMaterialStockDO::getBatchId, batchId);
        } else {
            wrapper.isNull(MesWmMaterialStockDO::getBatchId);
        }
        return selectOne(wrapper);
    }

    default List<MesWmMaterialStockDO> selectList(MesWmMaterialStockListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MesWmMaterialStockDO>()
                .eqIfPresent(MesWmMaterialStockDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(MesWmMaterialStockDO::getLocationId, reqVO.getLocationId())
                .eqIfPresent(MesWmMaterialStockDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(MesWmMaterialStockDO::getItemId, reqVO.getItemId())
                .eqIfPresent(MesWmMaterialStockDO::getBatchId, reqVO.getBatchId())
                .likeIfPresent(MesWmMaterialStockDO::getBatchCode, reqVO.getBatchCode())
                .geIfPresent(MesWmMaterialStockDO::getUpdateTime, reqVO.getStartTime())
                .leIfPresent(MesWmMaterialStockDO::getUpdateTime, reqVO.getEndTime())
                .ne(MesWmMaterialStockDO::getQuantity, BigDecimal.ZERO)
                .orderByAsc(MesWmMaterialStockDO::getReceiptTime));
    }

}
