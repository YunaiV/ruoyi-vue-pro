package cn.iocoder.yudao.module.wms.service.inbound.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.ErpProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.flow.WmsInboundItemFlowMapper;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 入库单详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
public class WmsInboundItemServiceImpl implements WmsInboundItemService {

    @Resource
    @Lazy
    private WmsInboundService inboundService;

    @Resource
    private WmsInboundItemMapper inboundItemMapper;

    @Resource
    private ErpProductApi productApi;

    @Resource
    @Lazy
    private WmsInboundItemFlowMapper inboundItemFlowMapper;

    /**
     * @sign : F55768BA65271F63
     */
    @Override
    public WmsInboundItemDO createInboundItem(WmsInboundItemSaveReqVO createReqVO) {
        if (inboundItemMapper.getByInboundIdAndProductId(createReqVO.getInboundId(), createReqVO.getProductId()) != null) {
            throw exception(INBOUND_ITEM_INBOUND_ID_PRODUCT_ID_DUPLICATE);
        }
        // 按 wms_inbound_item.inbound_id -> wms_inbound.id 的引用关系，校验存在性
        if (createReqVO.getInboundId() != null) {
            WmsInboundDO inbound = inboundService.getInbound(createReqVO.getInboundId());
            if (inbound == null) {
                throw exception(INBOUND_NOT_EXISTS);
            }
        }
        // 插入
        WmsInboundItemDO inboundItem = BeanUtils.toBean(createReqVO, WmsInboundItemDO.class);
        inboundItemMapper.insert(inboundItem);
        // 返回
        return inboundItem;
    }

    /**
     * @sign : 70F16E5A2203F0AA
     */
    @Override
    public WmsInboundItemDO updateInboundItem(WmsInboundItemSaveReqVO updateReqVO) {
        // 校验存在
        WmsInboundItemDO exists = validateInboundItemExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getInboundId(), exists.getInboundId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId())) {
            throw exception(INBOUND_ITEM_INBOUND_ID_PRODUCT_ID_DUPLICATE);
        }
        // 按 wms_inbound_item.inbound_id -> wms_inbound.id 的引用关系，校验存在性
        if (updateReqVO.getInboundId() != null) {
            WmsInboundDO inbound = inboundService.getInbound(updateReqVO.getInboundId());
            if (inbound == null) {
                throw exception(INBOUND_NOT_EXISTS);
            }
        }
        // 更新
        WmsInboundItemDO inboundItem = BeanUtils.toBean(updateReqVO, WmsInboundItemDO.class);
        inboundItemMapper.updateById(inboundItem);
        // 返回
        return inboundItem;
    }

    /**
     * @sign : EC9A13353E1B7B88
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInboundItem(Long id) {
        // 校验存在
        WmsInboundItemDO inboundItem = validateInboundItemExists(id);
        // 唯一索引去重
        inboundItem.setInboundId(inboundItemMapper.flagUKeyAsLogicDelete(inboundItem.getInboundId()));
        inboundItem.setProductId(inboundItemMapper.flagUKeyAsLogicDelete(inboundItem.getProductId()));
        inboundItemMapper.updateById(inboundItem);
        // 删除
        inboundItemMapper.deleteById(id);
    }

    /**
     * @sign : 1E3323E02C6F15FA
     */
    private WmsInboundItemDO validateInboundItemExists(Long id) {
        WmsInboundItemDO inboundItem = inboundItemMapper.selectById(id);
        if (inboundItem == null) {
            throw exception(INBOUND_ITEM_NOT_EXISTS);
        }
        return inboundItem;
    }

    @Override
    public WmsInboundItemDO getInboundItem(Long id) {
        return inboundItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInboundItemDO> getInboundItemPage(WmsInboundItemPageReqVO pageReqVO) {
        return inboundItemMapper.selectPage(pageReqVO);
    }

    /**
     * 按 inboundId 查询 WmsInboundItemDO
     */
    public List<WmsInboundItemDO> selectByInboundId(Long inboundId, int limit) {
        return inboundItemMapper.selectByInboundId(inboundId, limit);
    }

    @Override
    public void updateActualQuantity(List<WmsInboundItemSaveReqVO> updateReqVOList) {
        if (CollectionUtils.isEmpty(updateReqVOList)) {
            return;
        }
        Set<Long> inboundIds = StreamX.from(updateReqVOList).toSet(WmsInboundItemSaveReqVO::getInboundId);
        if (inboundIds.size() > 1) {
            throw exception(INBOUND_ITEM_INBOUND_ID_DUPLICATE);
        }
        Long inboundId = inboundIds.stream().findFirst().get();
        WmsInboundDO inboundDO = inboundService.validateInboundExists(inboundId);
        WmsInboundAuditStatus auditStatus = WmsInboundAuditStatus.parse(inboundDO.getAuditStatus());
        WmsInboundStatus inboundStatus = WmsInboundStatus.parse(inboundDO.getInboundStatus());
        // 除了审批中的情况，其它情况不允许修改实际入库量
        if (!auditStatus.matchAny(WmsInboundAuditStatus.AUDITING)) {
            throw exception(INBOUND_CAN_NOT_EDIT);
        }
        // 除了未入库的情况，其它情况不允许修改实际入库量
        if (!inboundStatus.matchAny(WmsInboundStatus.NONE)) {
            throw exception(INBOUND_CAN_NOT_EDIT);
        }
        Map<Long, WmsInboundItemSaveReqVO> updateReqVOMap = StreamX.from(updateReqVOList).toMap(WmsInboundItemSaveReqVO::getId);
        List<WmsInboundItemDO> inboundItemDOSInDB = inboundItemMapper.selectByIds(StreamX.from(updateReqVOList).toList(WmsInboundItemSaveReqVO::getId));
        for (WmsInboundItemDO itemDO : inboundItemDOSInDB) {
            WmsInboundItemSaveReqVO updateReqVO = updateReqVOMap.get(itemDO.getId());
            if(updateReqVO.getActualQty()==null || updateReqVO.getActualQty()<=0) {
                throw exception(INBOUND_ITEM_ACTUAL_QTY_ERROR);
            }
            itemDO.setActualQty(updateReqVO.getActualQty());
        }
        // 保存
        inboundItemMapper.updateBatch(inboundItemDOSInDB);
    }

    @Override
    public List<WmsInboundItemDO> selectByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return inboundItemMapper.selectByIds(ids);
    }

    @Override
    public void updateById(WmsInboundItemDO inboundItemDO) {
        inboundItemMapper.updateById(inboundItemDO);
    }

    @Override
    public PageResult<WmsInboundItemDO> getPickupPending(WmsPickupPendingPageReqVO pageReqVO) {
        return inboundItemMapper.getPickupPending(pageReqVO);
    }

    @Override
    public void assembleInbound(List<WmsInboundItemRespVO> itemList) {
        List<WmsInboundDO> inboundDOList = inboundService.selectByIds(StreamX.from(itemList).toList(WmsInboundItemRespVO::getInboundId));
        Map<Long, WmsInboundRespVO> inboundMap = StreamX.from(inboundDOList).toMap(WmsInboundDO::getId, inboundDO -> BeanUtils.toBean(inboundDO, WmsInboundRespVO.class));
        StreamX.from(itemList).assemble(inboundMap, WmsInboundItemRespVO::getInboundId, WmsInboundItemRespVO::setInbound);
    }

    @Override
    public List<WmsInboundItemDO> selectItemListHasAvailableQty(Long warehouseId, Long productId) {
        return inboundItemMapper.selectItemListHasAvailableQty(warehouseId, productId);
    }

    @Override
    public void saveItems(List<WmsInboundItemDO> itemsToUpdate, List<WmsInboundItemFlowDO> inboundItemFlowList) {
        // 保存流水
        inboundItemFlowMapper.insertBatch(inboundItemFlowList);
        Map<Long,WmsInboundItemFlowDO> flowDOMap=StreamX.from(inboundItemFlowList).toMap(WmsInboundItemFlowDO::getInboundItemId);

        for (WmsInboundItemDO itemDO : itemsToUpdate) {
            WmsInboundItemFlowDO flowDO=flowDOMap.get(itemDO.getId());
            itemDO.setLatestFlowId(flowDO.getId());
        }
        // 保存余量
        inboundItemMapper.updateBatch(itemsToUpdate);

    }

    @Override
    public void assembleProducts(List<WmsInboundItemRespVO> itemList) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(itemList).map(WmsInboundItemRespVO::getProductId).toList());
        Map<Long, ErpProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            ErpProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, ErpProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(itemList).assemble(productVOMap, WmsInboundItemRespVO::getProductId, WmsInboundItemRespVO::setProduct);
    }
}
