package cn.iocoder.yudao.module.mes.service.wm.returnsales;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.detail.MesWmReturnSalesDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.returnsales.MesWmReturnSalesDetailMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmReturnSalesStatusEnum;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 销售退货明细 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmReturnSalesDetailServiceImpl implements MesWmReturnSalesDetailService {

    @Resource
    private MesWmReturnSalesDetailMapper returnSalesDetailMapper;

    @Resource
    @Lazy
    private MesWmReturnSalesLineService returnSalesLineService;
    @Resource
    @Lazy
    private MesWmReturnSalesService returnSalesService;

    @Resource
    private MesWmWarehouseAreaService warehouseAreaService;

    @Resource
    @Lazy
    private MesWmMaterialStockService materialStockService;

    @Override
    public Long createReturnSalesDetail(MesWmReturnSalesDetailSaveReqVO createReqVO) {
        validateReturnSalesDetailSaveData(createReqVO);

        // 插入
        MesWmReturnSalesDetailDO detail = BeanUtils.toBean(createReqVO, MesWmReturnSalesDetailDO.class);
        returnSalesDetailMapper.insert(detail);
        return detail.getId();
    }

    @Override
    public void updateReturnSalesDetail(MesWmReturnSalesDetailSaveReqVO updateReqVO) {
        // 校验存在
        validateReturnSalesDetailExists(updateReqVO.getId());
        validateReturnSalesDetailSaveData(updateReqVO);

        // 更新
        MesWmReturnSalesDetailDO updateObj = BeanUtils.toBean(updateReqVO, MesWmReturnSalesDetailDO.class);
        returnSalesDetailMapper.updateById(updateObj);
    }

    @Override
    public void deleteReturnSalesDetail(Long id) {
        // 校验存在
        MesWmReturnSalesDetailDO detail = validateReturnSalesDetailExists(id);
        // 校验退货单为待上架状态才允许删除明细
        validateReturnSalesStatusApproved(detail.getReturnId());

        // 删除
        returnSalesDetailMapper.deleteById(id);
    }

    @Override
    public MesWmReturnSalesDetailDO getReturnSalesDetail(Long id) {
        return returnSalesDetailMapper.selectById(id);
    }

    @Override
    public List<MesWmReturnSalesDetailDO> getReturnSalesDetailListByLineId(Long lineId) {
        return returnSalesDetailMapper.selectListByLineId(lineId);
    }

    @Override
    public List<MesWmReturnSalesDetailDO> getReturnSalesDetailListByReturnId(Long returnId) {
        return returnSalesDetailMapper.selectListByReturnId(returnId);
    }

    @Override
    public void deleteReturnSalesDetailByReturnId(Long returnId) {
        returnSalesDetailMapper.deleteByReturnId(returnId);
    }

    @Override
    public void deleteReturnSalesDetailByLineId(Long lineId) {
        returnSalesDetailMapper.deleteByLineId(lineId);
    }

    private MesWmReturnSalesDetailDO validateReturnSalesDetailExists(Long id) {
        MesWmReturnSalesDetailDO detail = returnSalesDetailMapper.selectById(id);
        if (detail == null) {
            throw exception(WM_RETURN_SALES_DETAIL_NOT_EXISTS);
        }
        return detail;
    }

    /**
     * 校验退货单为待上架状态（明细操作仅允许在待上架状态）
     */
    private void validateReturnSalesStatusApproved(Long returnId) {
        MesWmReturnSalesDO returnSales = returnSalesService.validateReturnSalesExists(returnId);
        if (ObjUtil.notEqual(MesWmReturnSalesStatusEnum.APPROVED.getStatus(), returnSales.getStatus())) {
            throw exception(WM_RETURN_SALES_STATUS_NOT_APPROVED);
        }
    }

    /**
     * 校验保存时的关联数据
     */
    private void validateReturnSalesDetailSaveData(MesWmReturnSalesDetailSaveReqVO reqVO) {
        // 校验父数据存在
        MesWmReturnSalesLineDO line = returnSalesLineService.validateReturnSalesLineExists(reqVO.getLineId());
        // 校验退货单为待上架状态
        validateReturnSalesStatusApproved(line.getReturnId());
        // 校验仓库、库区、库位的关联关系
        warehouseAreaService.validateWarehouseAreaExists(
                reqVO.getWarehouseId(), reqVO.getLocationId(), reqVO.getAreaId());
        // 校验库位物料/批次混放规则
        materialStockService.checkAreaMixingRule(reqVO.getAreaId(), reqVO.getItemId(), reqVO.getBatchId());
        // 校验明细总数量不超过行数量（排除自身）
        validateDetailQuantityNotExceed(reqVO.getLineId(), reqVO.getQuantity(), reqVO.getId(), line);
    }

    /**
     * 校验明细总数量不超过行数量
     *
     * @param lineId          行 ID
     * @param newQuantity     本次新增/修改的数量
     * @param excludeDetailId 排除的明细 ID（更新时排除自身，新增时传 null）
     * @param line            退货单行
     */
    private void validateDetailQuantityNotExceed(Long lineId, BigDecimal newQuantity,
                                                  Long excludeDetailId, MesWmReturnSalesLineDO line) {
        // 计算已有明细总数量（排除自身）
        List<MesWmReturnSalesDetailDO> details = returnSalesDetailMapper.selectListByLineId(lineId);
        BigDecimal existingTotal = CollectionUtils.getSumValue(details,
                detail -> excludeDetailId != null && excludeDetailId.equals(detail.getId())
                        ? BigDecimal.ZERO : detail.getQuantity(),
                BigDecimal::add, BigDecimal.ZERO);
        // 校验：已有 + 本次 <= 行数量
        if (existingTotal.add(newQuantity).compareTo(line.getQuantity()) > 0) {
            throw exception(WM_RETURN_SALES_DETAIL_QUANTITY_EXCEED);
        }
    }

}
