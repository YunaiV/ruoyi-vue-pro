package cn.iocoder.yudao.module.wms.service.inbound.item;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 入库单详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsInboundItemServiceImpl implements WmsInboundItemService {

    @Resource
    private WmsInboundItemMapper inboundItemMapper;

    /**
     * @sign : C98680CDD99F35BD
     */
    @Override
    public WmsInboundItemDO createInboundItem(WmsInboundItemSaveReqVO createReqVO) {
        if (inboundItemMapper.getByInboundIdAndProductId(createReqVO.getInboundId(), createReqVO.getProductId()) != null) {
            throw exception(INBOUND_ITEM_INBOUND_ID_PRODUCT_ID_DUPLICATE);
        }
        if (inboundItemMapper.getByInboundIdAndProductSku(createReqVO.getInboundId(), createReqVO.getProductSku()) != null) {
            throw exception(INBOUND_ITEM_INBOUND_ID_PRODUCT_SKU_DUPLICATE);
        }
        // 插入
        WmsInboundItemDO inboundItem = BeanUtils.toBean(createReqVO, WmsInboundItemDO.class);
        inboundItemMapper.insert(inboundItem);
        // 返回
        return inboundItem;
    }

    /**
     * @sign : 43E659CF6C2B1136
     */
    @Override
    public WmsInboundItemDO updateInboundItem(WmsInboundItemSaveReqVO updateReqVO) {
        // 校验存在
        WmsInboundItemDO exists = validateInboundItemExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getInboundId(), exists.getInboundId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId())) {
            throw exception(INBOUND_ITEM_INBOUND_ID_PRODUCT_ID_DUPLICATE);
        }
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getInboundId(), exists.getInboundId()) && Objects.equals(updateReqVO.getProductSku(), exists.getProductSku())) {
            throw exception(INBOUND_ITEM_INBOUND_ID_PRODUCT_SKU_DUPLICATE);
        }
        // 更新
        WmsInboundItemDO inboundItem = BeanUtils.toBean(updateReqVO, WmsInboundItemDO.class);
        inboundItemMapper.updateById(inboundItem);
        // 返回
        return inboundItem;
    }

    /**
     * @sign : D1E17D3946811E77
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInboundItem(Long id) {
        // 校验存在
        WmsInboundItemDO inboundItem = validateInboundItemExists(id);
        // 唯一索引去重
        inboundItem.setInboundId(inboundItemMapper.flagUKeyAsLogicDelete(inboundItem.getInboundId()));
        inboundItem.setProductId(inboundItemMapper.flagUKeyAsLogicDelete(inboundItem.getProductId()));
        inboundItem.setProductSku(inboundItemMapper.flagUKeyAsLogicDelete(inboundItem.getProductSku()));
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
}