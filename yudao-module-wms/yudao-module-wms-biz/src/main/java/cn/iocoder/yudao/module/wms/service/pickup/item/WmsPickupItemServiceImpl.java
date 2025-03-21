package cn.iocoder.yudao.module.wms.service.pickup.item;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.pickup.item.WmsPickupItemMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

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
}