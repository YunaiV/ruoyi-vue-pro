package cn.iocoder.yudao.module.wms.service.stock.bin.move.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.move.item.WmsStockBinMoveItemMapper;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.WmsStockBinMoveService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_MOVE_ITEM_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_MOVE_ITEM_NOT_EXISTS;

/**
 * 库位移动详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockBinMoveItemServiceImpl implements WmsStockBinMoveItemService {

    @Resource
    private WmsStockBinMoveItemMapper stockBinMoveItemMapper;

    @Autowired
    @Lazy
    private WmsWarehouseBinService warehouseBinService;

    @Autowired
    @Lazy
    private WmsStockBinMoveService stockBinMoveService;

    @Resource
    private ErpProductApi productApi;

    /**
     * @sign : C50CC3A83D97A3B3
     */
    @Override
    public WmsStockBinMoveItemDO createStockBinMoveItem(WmsStockBinMoveItemSaveReqVO createReqVO) {
        if (stockBinMoveItemMapper.getByUk(createReqVO.getBinMoveId(), createReqVO.getProductId(), createReqVO.getFromBinId(), createReqVO.getToBinId()) != null) {
            throw exception(STOCK_BIN_MOVE_ITEM_EXISTS);
        }
        // 插入
        WmsStockBinMoveItemDO stockBinMoveItem = BeanUtils.toBean(createReqVO, WmsStockBinMoveItemDO.class);
        stockBinMoveItemMapper.insert(stockBinMoveItem);
        // 返回
        return stockBinMoveItem;
    }

    /**
     * @sign : E840376DE7D4CCD9
     */
    @Override
    public WmsStockBinMoveItemDO updateStockBinMoveItem(WmsStockBinMoveItemSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockBinMoveItemDO exists = validateStockBinMoveItemExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getBinMoveId(), exists.getBinMoveId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId()) && Objects.equals(updateReqVO.getFromBinId(), exists.getFromBinId()) && Objects.equals(updateReqVO.getToBinId(), exists.getToBinId())) {
            throw exception(STOCK_BIN_MOVE_ITEM_EXISTS);
        }
        // 更新
        WmsStockBinMoveItemDO stockBinMoveItem = BeanUtils.toBean(updateReqVO, WmsStockBinMoveItemDO.class);
        stockBinMoveItemMapper.updateById(stockBinMoveItem);
        // 返回
        return stockBinMoveItem;
    }

    /**
     * @sign : 7588E6DEA13FF799
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockBinMoveItem(Long id) {
        // 校验存在
        WmsStockBinMoveItemDO stockBinMoveItem = validateStockBinMoveItemExists(id);
        // 唯一索引去重
        stockBinMoveItem.setBinMoveId(stockBinMoveItemMapper.flagUKeyAsLogicDelete(stockBinMoveItem.getBinMoveId()));
        stockBinMoveItem.setProductId(stockBinMoveItemMapper.flagUKeyAsLogicDelete(stockBinMoveItem.getProductId()));
        stockBinMoveItem.setFromBinId(stockBinMoveItemMapper.flagUKeyAsLogicDelete(stockBinMoveItem.getFromBinId()));
        stockBinMoveItem.setToBinId(stockBinMoveItemMapper.flagUKeyAsLogicDelete(stockBinMoveItem.getToBinId()));
        stockBinMoveItemMapper.updateById(stockBinMoveItem);
        // 删除
        stockBinMoveItemMapper.deleteById(id);
    }

    /**
     * @sign : F8648A012C510A3D
     */
    private WmsStockBinMoveItemDO validateStockBinMoveItemExists(Long id) {
        WmsStockBinMoveItemDO stockBinMoveItem = stockBinMoveItemMapper.selectById(id);
        if (stockBinMoveItem == null) {
            throw exception(STOCK_BIN_MOVE_ITEM_NOT_EXISTS);
        }
        return stockBinMoveItem;
    }

    @Override
    public WmsStockBinMoveItemDO getStockBinMoveItem(Long id) {
        return stockBinMoveItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockBinMoveItemDO> getStockBinMoveItemPage(WmsStockBinMoveItemPageReqVO pageReqVO) {
        return stockBinMoveItemMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsStockBinMoveItemDO
     */
    public List<WmsStockBinMoveItemDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return stockBinMoveItemMapper.selectByIds(idList);
    }

    @Override
    public List<WmsStockBinMoveItemDO> selectByBinMoveId(Long binMoveId) {
        return stockBinMoveItemMapper.selectByBinMoveId(binMoveId);
    }

    @Override
    public void assembleBin(List<WmsStockBinMoveItemRespVO> stockBinMoveItemList) {
        List<Long> ids=new ArrayList<>();
        ids.addAll(StreamX.from(stockBinMoveItemList).toList(WmsStockBinMoveItemRespVO::getFromBinId).stream().distinct().toList());
        ids.addAll(StreamX.from(stockBinMoveItemList).toList(WmsStockBinMoveItemRespVO::getToBinId).stream().distinct().toList());
        List<WmsWarehouseBinDO> binDOList = warehouseBinService.selectByIds(ids);

        List<WmsWarehouseBinRespVO> binVOList = BeanUtils.toBean(binDOList, WmsWarehouseBinRespVO.class);
        StreamX.from(stockBinMoveItemList).assemble(binVOList, WmsWarehouseBinRespVO::getId, WmsStockBinMoveItemRespVO::getFromBinId,WmsStockBinMoveItemRespVO::setFromBin);
        StreamX.from(stockBinMoveItemList).assemble(binVOList, WmsWarehouseBinRespVO::getId, WmsStockBinMoveItemRespVO::getToBinId,WmsStockBinMoveItemRespVO::setToBin);
    }

    @Override
    public void assembleProduct(List<WmsStockBinMoveItemRespVO> stockBinMoveItemList) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(stockBinMoveItemList).map(WmsStockBinMoveItemRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(stockBinMoveItemList).assemble(productVOMap, WmsStockBinMoveItemRespVO::getProductId, WmsStockBinMoveItemRespVO::setProduct);
    }

    @Override
    public void assembleBinMove(List<WmsStockBinMoveItemRespVO> list) {
        List<Long> ids=StreamX.from(list).toList(WmsStockBinMoveItemRespVO::getBinMoveId).stream().distinct().toList();
        List<WmsStockBinMoveDO> stockBinMoveDOS = stockBinMoveService.selectByIds(ids);

        List<WmsStockBinMoveSimpleRespVO> binMoveVOList = BeanUtils.toBean(stockBinMoveDOS, WmsStockBinMoveSimpleRespVO.class);
        StreamX.from(list).assemble(binMoveVOList, WmsStockBinMoveSimpleRespVO::getId, WmsStockBinMoveItemRespVO::getBinMoveId,WmsStockBinMoveItemRespVO::setBinMove);
    }


}
