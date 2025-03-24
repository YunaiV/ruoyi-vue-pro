package cn.iocoder.yudao.module.wms.service.outbound.item;

import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.ErpProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.item.WmsOutboundItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.outbound.item.WmsOutboundItemMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

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
        Map<Long, ErpProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            ErpProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, ErpProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(itemList).assemble(productVOMap, WmsOutboundItemRespVO::getProductId, WmsOutboundItemRespVO::setProduct);
    }
}