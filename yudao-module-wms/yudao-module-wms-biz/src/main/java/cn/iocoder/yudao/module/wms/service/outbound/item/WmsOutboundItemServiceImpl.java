package cn.iocoder.yudao.module.wms.service.outbound.item;

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

    @Override
    public Long createOutboundItem(WmsOutboundItemSaveReqVO createReqVO) {
        // 插入
        WmsOutboundItemDO outboundItem = BeanUtils.toBean(createReqVO, WmsOutboundItemDO.class);
        outboundItemMapper.insert(outboundItem);
        // 返回
        return outboundItem.getId();
    }

    @Override
    public void updateOutboundItem(WmsOutboundItemSaveReqVO updateReqVO) {
        // 校验存在
        validateOutboundItemExists(updateReqVO.getId());
        // 更新
        WmsOutboundItemDO updateObj = BeanUtils.toBean(updateReqVO, WmsOutboundItemDO.class);
        outboundItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteOutboundItem(Long id) {
        // 校验存在
        validateOutboundItemExists(id);
        // 删除
        outboundItemMapper.deleteById(id);
    }

    private void validateOutboundItemExists(Long id) {
        if (outboundItemMapper.selectById(id) == null) {
            //throw exception(OUTBOUND_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public WmsOutboundItemDO getOutboundItem(Long id) {
        return outboundItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsOutboundItemDO> getOutboundItemPage(WmsOutboundItemPageReqVO pageReqVO) {
        return outboundItemMapper.selectPage(pageReqVO);
    }

}