package cn.iocoder.yudao.module.mes.service.wm.productsales;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.detail.MesWmProductSalesDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productsales.MesWmProductSalesDetailMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 销售出库明细 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmProductSalesDetailServiceImpl implements MesWmProductSalesDetailService {

    @Resource
    private MesWmProductSalesDetailMapper productSalesDetailMapper;

    @Resource
    private MesMdItemService itemService;
    @Resource
    @Lazy
    private MesWmProductSalesLineService productSalesLineService;
    @Resource
    private MesWmMaterialStockService materialStockService;
    @Resource
    private MesWmWarehouseAreaService warehouseAreaService;

    @Override
    public Long createProductSalesDetail(MesWmProductSalesDetailSaveReqVO createReqVO) {
        // 校验数据
        validateProductSalesDetailSaveData(createReqVO);

        // 插入
        MesWmProductSalesDetailDO detail = BeanUtils.toBean(createReqVO, MesWmProductSalesDetailDO.class);
        productSalesDetailMapper.insert(detail);
        return detail.getId();
    }

    @Override
    public void updateProductSalesDetail(MesWmProductSalesDetailSaveReqVO updateReqVO) {
        // 校验存在
        validateProductSalesDetailExists(updateReqVO.getId());
        // 校验数据
        validateProductSalesDetailSaveData(updateReqVO);

        // 更新
        MesWmProductSalesDetailDO updateObj = BeanUtils.toBean(updateReqVO, MesWmProductSalesDetailDO.class);
        productSalesDetailMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductSalesDetail(Long id) {
        // 校验存在
        validateProductSalesDetailExists(id);
        // 删除
        productSalesDetailMapper.deleteById(id);
    }

    @Override
    public MesWmProductSalesDetailDO getProductSalesDetail(Long id) {
        return productSalesDetailMapper.selectById(id);
    }

    @Override
    public List<MesWmProductSalesDetailDO> getProductSalesDetailListBySalesId(Long salesId) {
        return productSalesDetailMapper.selectListBySalesId(salesId);
    }

    @Override
    public List<MesWmProductSalesDetailDO> getProductSalesDetailListByLineId(Long lineId) {
        return productSalesDetailMapper.selectListByLineId(lineId);
    }

    @Override
    public void deleteProductSalesDetailByLineId(Long lineId) {
        productSalesDetailMapper.deleteByLineId(lineId);
    }

    @Override
    public void deleteProductSalesDetailBySalesId(Long salesId) {
        productSalesDetailMapper.deleteBySalesId(salesId);
    }

    private MesWmProductSalesDetailDO validateProductSalesDetailExists(Long id) {
        MesWmProductSalesDetailDO detail = productSalesDetailMapper.selectById(id);
        if (detail == null) {
            throw exception(WM_PRODUCT_SALES_DETAIL_NOT_EXISTS);
        }
        return detail;
    }

    private void validateProductSalesDetailSaveData(MesWmProductSalesDetailSaveReqVO reqVO) {
        // 校验父数据（行）存在
        MesWmProductSalesLineDO line = productSalesLineService.getProductSalesLine(reqVO.getLineId());
        if (line == null) {
            throw exception(WM_PRODUCT_SALES_LINE_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(line.getSalesId(), reqVO.getSalesId())) {
            throw exception(WM_PRODUCT_SALES_DETAIL_LINE_NOT_MATCH);
        }
        // 校验物料存在
        itemService.validateItemExistsAndEnable(reqVO.getItemId());
        if (ObjUtil.notEqual(line.getItemId(), reqVO.getItemId())) {
            throw exception(WM_PRODUCT_SALES_DETAIL_ITEM_MISMATCH);
        }
        // 校验库位存在
        warehouseAreaService.validateWarehouseAreaExists(
                reqVO.getWarehouseId(), reqVO.getLocationId(), reqVO.getAreaId());
        // 校验库存记录存在且物料一致
        materialStockService.validateSelectedStock(
                reqVO.getMaterialStockId(), reqVO.getItemId(), reqVO.getBatchId(), reqVO.getBatchCode(),
                reqVO.getWarehouseId(), reqVO.getLocationId(), reqVO.getAreaId(), reqVO.getQuantity());
    }

}
