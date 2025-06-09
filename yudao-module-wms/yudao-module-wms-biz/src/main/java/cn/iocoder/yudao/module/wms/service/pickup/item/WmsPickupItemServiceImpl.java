package cn.iocoder.yudao.module.wms.service.pickup.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo.WmsPickupItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo.WmsPickupItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo.WmsPickupItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.module.wms.dal.mysql.pickup.item.WmsPickupItemMapper;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import de.danielbechler.util.Collections;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.PICKUP_ITEM_NOT_EXISTS;

/**
 * 拣货单详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsPickupItemServiceImpl implements WmsPickupItemService {

    @Resource
    private WmsPickupItemMapper pickupItemMapper;

    @Resource
    private ErpProductApi productApi;

    @Resource
    @Lazy
    private WmsInboundService inboundService;
    /**
     * @sign : B28631E68735E52E
     */
    @Override
    public WmsPickupItemDO createPickupItem(WmsPickupItemSaveReqVO createReqVO) {
        // 插入
        WmsPickupItemDO pickupItem = BeanUtils.toBean(createReqVO, WmsPickupItemDO.class);
        pickupItemMapper.insert(pickupItem);
        // 返回
        return pickupItem;
    }

    /**
     * @sign : C8B253A9DA20BCC0
     */
    @Override
    public WmsPickupItemDO updatePickupItem(WmsPickupItemSaveReqVO updateReqVO) {
        // 校验存在
        WmsPickupItemDO exists = validatePickupItemExists(updateReqVO.getId());
        // 更新
        WmsPickupItemDO pickupItem = BeanUtils.toBean(updateReqVO, WmsPickupItemDO.class);
        pickupItemMapper.updateById(pickupItem);
        // 返回
        return pickupItem;
    }

    /**
     * @sign : A1AC7C5E9A6538A4
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePickupItem(Long id) {
        // 校验存在
        WmsPickupItemDO pickupItem = validatePickupItemExists(id);
        // 删除
        pickupItemMapper.deleteById(id);
    }

    /**
     * @sign : C1DD5086E48762F0
     */
    private WmsPickupItemDO validatePickupItemExists(Long id) {
        WmsPickupItemDO pickupItem = pickupItemMapper.selectById(id);
        if (pickupItem == null) {
            throw exception(PICKUP_ITEM_NOT_EXISTS);
        }
        return pickupItem;
    }

    @Override
    public WmsPickupItemDO getPickupItem(Long id) {
        return pickupItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsPickupItemDO> getPickupItemPage(WmsPickupItemPageReqVO pageReqVO) {
        return pickupItemMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsPickupItemDO> selectByPickupId(Long id) {
        return pickupItemMapper.selectByPickupId(id);
    }

    @Override
    public void assembleProduct(List<WmsPickupItemRespVO> itemList) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(itemList).map(WmsPickupItemRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(itemList).assemble(productVOMap, WmsPickupItemRespVO::getProductId, WmsPickupItemRespVO::setProduct);
    }

    @Override
    public void assembleInbound(List<WmsPickupItemRespVO> itemList) {
        List<WmsInboundDO> inboundDOList = inboundService.selectByIds(StreamX.from(itemList).toList(WmsPickupItemRespVO::getInboundId));
        Map<Long, WmsInboundSimpleRespVO> inboundMap = StreamX.from(inboundDOList).toMap(WmsInboundDO::getId, inboundDO -> BeanUtils.toBean(inboundDO, WmsInboundSimpleRespVO.class));
        StreamX.from(itemList).assemble(inboundMap, WmsPickupItemRespVO::getInboundId, WmsPickupItemRespVO::setInbound);
    }

    @Override
    public List<WmsPickupItemDO> getPickupItemListByInboundItemIds(Set<Long> inboundItemIds) {
        if(Collections.isEmpty(inboundItemIds)) {
            return List.of();
        }
        return pickupItemMapper.getPickupItemListByInboundItemIds(inboundItemIds);
    }
}
