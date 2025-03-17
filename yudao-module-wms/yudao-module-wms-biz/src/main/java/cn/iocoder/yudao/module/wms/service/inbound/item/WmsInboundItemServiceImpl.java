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

    @Override
    public Long createInboundItem(WmsInboundItemSaveReqVO createReqVO) {
        // 插入
        WmsInboundItemDO inboundItem = BeanUtils.toBean(createReqVO, WmsInboundItemDO.class);
        inboundItemMapper.insert(inboundItem);
        // 返回
        return inboundItem.getId();
    }

    @Override
    public void updateInboundItem(WmsInboundItemSaveReqVO updateReqVO) {
        // 校验存在
        validateInboundItemExists(updateReqVO.getId());
        // 更新
        WmsInboundItemDO updateObj = BeanUtils.toBean(updateReqVO, WmsInboundItemDO.class);
        inboundItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteInboundItem(Long id) {
        // 校验存在
        validateInboundItemExists(id);
        // 删除
        inboundItemMapper.deleteById(id);
    }

    private void validateInboundItemExists(Long id) {
        if (inboundItemMapper.selectById(id) == null) {
            //throw exception(INBOUND_ITEM_NOT_EXISTS);
        }
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