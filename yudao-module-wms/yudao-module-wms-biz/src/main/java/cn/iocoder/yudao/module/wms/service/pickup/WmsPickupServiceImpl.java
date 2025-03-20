package cn.iocoder.yudao.module.wms.service.pickup;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.pickup.WmsPickupMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;

/**
 * 拣货单 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsPickupServiceImpl implements WmsPickupService {

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsPickupMapper pickupMapper;

    /**
     * @sign : 4F6BD1A6B0D91B00
     */
    @Override
    public WmsPickupDO createPickup(WmsPickupSaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.PICKUP_NO_PREFIX, PICKUP_NOT_EXISTS);
        createReqVO.setNo(no);
        if (pickupMapper.getByNo(createReqVO.getNo()) != null) {
            throw exception(PICKUP_NO_DUPLICATE);
        }
        // 插入
        WmsPickupDO pickup = BeanUtils.toBean(createReqVO, WmsPickupDO.class);
        pickupMapper.insert(pickup);
        // 返回
        return pickup;
    }

    /**
     * @sign : 5F6E31A8C9B740D4
     */
    @Override
    public WmsPickupDO updatePickup(WmsPickupSaveReqVO updateReqVO) {
        // 校验存在
        WmsPickupDO exists = validatePickupExists(updateReqVO.getId());
        // 单据号不允许被修改
        updateReqVO.setNo(exists.getNo());
        // 更新
        WmsPickupDO pickup = BeanUtils.toBean(updateReqVO, WmsPickupDO.class);
        pickupMapper.updateById(pickup);
        // 返回
        return pickup;
    }

    /**
     * @sign : E178F96C953E3D97
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePickup(Long id) {
        // 校验存在
        WmsPickupDO pickup = validatePickupExists(id);
        // 唯一索引去重
        pickup.setNo(pickupMapper.flagUKeyAsLogicDelete(pickup.getNo()));
        pickupMapper.updateById(pickup);
        // 删除
        pickupMapper.deleteById(id);
    }

    /**
     * @sign : ACE5C762DE808407
     */
    private WmsPickupDO validatePickupExists(Long id) {
        WmsPickupDO pickup = pickupMapper.selectById(id);
        if (pickup == null) {
            throw exception(PICKUP_NOT_EXISTS);
        }
        return pickup;
    }

    @Override
    public WmsPickupDO getPickup(Long id) {
        return pickupMapper.selectById(id);
    }

    @Override
    public PageResult<WmsPickupDO> getPickupPage(WmsPickupPageReqVO pageReqVO) {
        return pickupMapper.selectPage(pageReqVO);
    }
}