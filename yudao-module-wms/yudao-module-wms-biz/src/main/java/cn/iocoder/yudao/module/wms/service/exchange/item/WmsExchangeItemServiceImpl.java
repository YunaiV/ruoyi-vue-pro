package cn.iocoder.yudao.module.wms.service.exchange.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo.WmsExchangeItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo.WmsExchangeItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo.WmsExchangeItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.item.WmsExchangeItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.dal.mysql.exchange.item.WmsExchangeItemMapper;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.EXCHANGE_ITEM_NOT_EXISTS;

/**
 * 良次换货详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsExchangeItemServiceImpl implements WmsExchangeItemService {

    @Resource
    private WmsExchangeItemMapper exchangeItemMapper;

    @Resource
    private ErpProductApi productApi;

    @Autowired
    @Lazy
    private WmsWarehouseBinService warehouseBinService;

    /**
     * @sign : D8D21927B4E9471E
     */
    @Override
    public WmsExchangeItemDO createExchangeItem(WmsExchangeItemSaveReqVO createReqVO) {
        // 插入
        WmsExchangeItemDO exchangeItem = BeanUtils.toBean(createReqVO, WmsExchangeItemDO.class);
        exchangeItemMapper.insert(exchangeItem);
        // 返回
        return exchangeItem;
    }

    /**
     * @sign : B57F47DB56BDCCB1
     */
    @Override
    public WmsExchangeItemDO updateExchangeItem(WmsExchangeItemSaveReqVO updateReqVO) {
        // 校验存在
        WmsExchangeItemDO exists = validateExchangeItemExists(updateReqVO.getId());
        // 更新
        WmsExchangeItemDO exchangeItem = BeanUtils.toBean(updateReqVO, WmsExchangeItemDO.class);
        exchangeItemMapper.updateById(exchangeItem);
        // 返回
        return exchangeItem;
    }

    /**
     * @sign : E254BEA012EE672A
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExchangeItem(Long id) {
        // 校验存在
        WmsExchangeItemDO exchangeItem = validateExchangeItemExists(id);
        // 删除
        exchangeItemMapper.deleteById(id);
    }

    /**
     * @sign : 74CED14CB2129470
     */
    private WmsExchangeItemDO validateExchangeItemExists(Long id) {
        WmsExchangeItemDO exchangeItem = exchangeItemMapper.selectById(id);
        if (exchangeItem == null) {
            throw exception(EXCHANGE_ITEM_NOT_EXISTS);
        }
        return exchangeItem;
    }

    @Override
    public WmsExchangeItemDO getExchangeItem(Long id) {
        return exchangeItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsExchangeItemDO> getExchangeItemPage(WmsExchangeItemPageReqVO pageReqVO) {
        return exchangeItemMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsExchangeItemDO
     */
    @Override
    public List<WmsExchangeItemDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return exchangeItemMapper.selectByIds(idList);
    }

    /**
     * 根据换货单ID查询换货详情
     */
    @Override
    public List<WmsExchangeItemDO> selectByExchangeId(Long id) {
        return exchangeItemMapper.selectByExchangeId(id);
    }

    /**
     * 装配仓位
     **/
    @Override
    public void assembleBins(List<WmsExchangeItemRespVO> itemList) {

        Set<Long> binIds = new HashSet<>();
        binIds.addAll(StreamX.from(itemList).toList(WmsExchangeItemRespVO::getFromBinId).stream().distinct().toList());
        binIds.addAll(StreamX.from(itemList).toList(WmsExchangeItemRespVO::getToBinId).stream().distinct().toList());

        List<WmsWarehouseBinDO> binDOList = warehouseBinService.selectByIds(binIds);
        List<WmsWarehouseBinRespVO> binVOList = BeanUtils.toBean(binDOList, WmsWarehouseBinRespVO.class);

        StreamX.from(itemList).assemble(binVOList, WmsWarehouseBinRespVO::getId, WmsExchangeItemRespVO::getFromBinId, WmsExchangeItemRespVO::setFromBin);
        StreamX.from(itemList).assemble(binVOList, WmsWarehouseBinRespVO::getId, WmsExchangeItemRespVO::getToBinId, WmsExchangeItemRespVO::setToBin);

    }

    /**
     * 装配产品
     **/
    @Override
    public void assembleProduct(List<WmsExchangeItemRespVO> itemList) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(itemList).map(WmsExchangeItemRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(itemList).assemble(productVOMap, WmsExchangeItemRespVO::getProductId, WmsExchangeItemRespVO::setProduct);
    }
}
