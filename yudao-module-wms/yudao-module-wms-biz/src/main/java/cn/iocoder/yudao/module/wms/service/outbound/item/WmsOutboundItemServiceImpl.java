package cn.iocoder.yudao.module.wms.service.outbound.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.item.WmsOutboundItemDO;
import cn.iocoder.yudao.module.wms.dal.mysql.outbound.item.WmsOutboundItemMapper;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import java.util.List;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 出库单详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsOutboundItemServiceImpl implements WmsOutboundItemService {

    @Resource
    private WmsOutboundItemMapper outboundItemMapper;

    @Resource
    @Lazy
    private WmsOutboundService outboundService;

    @Resource
    private ErpProductApi productApi;

    /**
     * @sign : 07E3106EC549C08B
     */
    @Override
    public WmsOutboundItemDO createOutboundItem(WmsOutboundItemSaveReqVO createReqVO) {
        if (outboundItemMapper.getByOutboundIdAndProductId(createReqVO.getOutboundId(), createReqVO.getProductId()) != null) {
            throw exception(OUTBOUND_ITEM_OUTBOUND_ID_PRODUCT_ID_DUPLICATE);
        }
        // 插入
        WmsOutboundItemDO outboundItem = BeanUtils.toBean(createReqVO, WmsOutboundItemDO.class);
        outboundItemMapper.insert(outboundItem);
        // 返回
        return outboundItem;
    }

    /**
     * @sign : DE4057EE0E9C9089
     */
    @Override
    public WmsOutboundItemDO updateOutboundItem(WmsOutboundItemSaveReqVO updateReqVO) {
        // 校验存在
        WmsOutboundItemDO exists = validateOutboundItemExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getOutboundId(), exists.getOutboundId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId())) {
            throw exception(OUTBOUND_ITEM_OUTBOUND_ID_PRODUCT_ID_DUPLICATE);
        }
        // 更新
        WmsOutboundItemDO outboundItem = BeanUtils.toBean(updateReqVO, WmsOutboundItemDO.class);
        outboundItemMapper.updateById(outboundItem);
        // 返回
        return outboundItem;
    }

    /**
     * @sign : 0FCAD168C4B2848E
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOutboundItem(Long id) {
        // 校验存在
        WmsOutboundItemDO outboundItem = validateOutboundItemExists(id);
        // 唯一索引去重
        outboundItem.setOutboundId(outboundItemMapper.flagUKeyAsLogicDelete(outboundItem.getOutboundId()));
        outboundItem.setProductId(outboundItemMapper.flagUKeyAsLogicDelete(outboundItem.getProductId()));
        outboundItemMapper.updateById(outboundItem);
        // 删除
        outboundItemMapper.deleteById(id);
    }

    /**
     * @sign : 2663D514EBB0E44D
     */
    private WmsOutboundItemDO validateOutboundItemExists(Long id) {
        WmsOutboundItemDO outboundItem = outboundItemMapper.selectById(id);
        if (outboundItem == null) {
            throw exception(OUTBOUND_ITEM_NOT_EXISTS);
        }
        return outboundItem;
    }

    @Override
    public WmsOutboundItemDO getOutboundItem(Long id) {
        return outboundItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsOutboundItemDO> getOutboundItemPage(WmsOutboundItemPageReqVO pageReqVO) {
        return outboundItemMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsOutboundItemDO> selectByOutboundId(Long outboundId) {
        return outboundItemMapper.selectByOutboundId(outboundId);
    }

    @Override
    public void assembleProducts(List<WmsOutboundItemRespVO> itemList) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(itemList).map(WmsOutboundItemRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(itemList).assemble(productVOMap, WmsOutboundItemRespVO::getProductId, WmsOutboundItemRespVO::setProduct);
    }

    @Override
    public void updateActualQuantity(List<WmsOutboundItemSaveReqVO> updateReqVOList) {
        if (CollectionUtils.isEmpty(updateReqVOList)) {
            return;
        }
        Set<Long> outboundIds = StreamX.from(updateReqVOList).toSet(WmsOutboundItemSaveReqVO::getOutboundId);
        if (outboundIds.size() > 1) {
            throw exception(OUTBOUND_ITEM_OUTBOUND_ID_DUPLICATE);
        }
        Long outboundId = outboundIds.stream().findFirst().get();
        WmsOutboundDO outboundDO = outboundService.validateOutboundExists(outboundId);
        WmsOutboundAuditStatus auditStatus = WmsOutboundAuditStatus.parse(outboundDO.getAuditStatus());
        WmsOutboundStatus wmsOutboundStatus = WmsOutboundStatus.parse(outboundDO.getOutboundStatus());
        // 审批通过后，设置实际出库量
        if (!auditStatus.matchAny(WmsOutboundAuditStatus.PASS)) {
            throw exception(OUTBOUND_CAN_NOT_EDIT);
        }
        // 除了未入库的情况，其它情况不允许修改实际入库量
        if (!wmsOutboundStatus.matchAny(WmsOutboundStatus.NONE)) {
            throw exception(INBOUND_CAN_NOT_EDIT);
        }
        // 校验数量
        Map<Long, WmsOutboundItemSaveReqVO> updateReqVOMap = StreamX.from(updateReqVOList).toMap(WmsOutboundItemSaveReqVO::getId);
        List<WmsOutboundItemDO> outboundItemDOSInDB = outboundItemMapper.selectByIds(StreamX.from(updateReqVOList).toList(WmsOutboundItemSaveReqVO::getId));
        for (WmsOutboundItemDO itemDO : outboundItemDOSInDB) {
            WmsOutboundItemSaveReqVO updateReqVO = updateReqVOMap.get(itemDO.getId());
            if (updateReqVO.getActualQty() == null || updateReqVO.getActualQty() <= 0) {
                throw exception(OUTBOUND_ITEM_ACTUAL_QTY_ERROR);
            }
            itemDO.setActualQty(updateReqVO.getActualQty());
        }
        // 保存
        outboundItemMapper.updateBatch(outboundItemDOSInDB);
    }

    /**
     * 按 ID 集合查询 WmsOutboundItemDO
     */
    public List<WmsOutboundItemDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return outboundItemMapper.selectByIds(idList);
    }
}