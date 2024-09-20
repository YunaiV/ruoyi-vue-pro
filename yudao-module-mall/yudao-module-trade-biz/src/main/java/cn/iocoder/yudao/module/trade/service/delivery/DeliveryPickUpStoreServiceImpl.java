package cn.iocoder.yudao.module.trade.service.delivery;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpBindStoreStaffIdReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStoreCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStorePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStoreUpdateReqVO;
import cn.iocoder.yudao.module.trade.convert.delivery.DeliveryPickUpStoreConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreStaffDO;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryPickUpStoreMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryPickUpStoreStaffMapper;
import com.mchange.lang.LongUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PICK_UP_STORE_NOT_EXISTS;

/**
 * 自提门店 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class DeliveryPickUpStoreServiceImpl implements DeliveryPickUpStoreService {

    @Resource
    private DeliveryPickUpStoreMapper deliveryPickUpStoreMapper;
    @Resource
    private DeliveryPickUpStoreStaffMapper deliveryPickUpStoreStaffMapper;

    @Override
    public Long createDeliveryPickUpStore(DeliveryPickUpStoreCreateReqVO createReqVO) {
        // 插入
        DeliveryPickUpStoreDO deliveryPickUpStore = DeliveryPickUpStoreConvert.INSTANCE.convert(createReqVO);
        deliveryPickUpStoreMapper.insert(deliveryPickUpStore);
        // 返回
        return deliveryPickUpStore.getId();
    }

    @Override
    public void updateDeliveryPickUpStore(DeliveryPickUpStoreUpdateReqVO updateReqVO) {
        // 校验存在
        validateDeliveryPickUpStoreExists(updateReqVO.getId());
        // 更新
        DeliveryPickUpStoreDO updateObj = DeliveryPickUpStoreConvert.INSTANCE.convert(updateReqVO);
        deliveryPickUpStoreMapper.updateById(updateObj);
    }

    @Override
    public void deleteDeliveryPickUpStore(Long id) {
        // 校验存在
        validateDeliveryPickUpStoreExists(id);
        // 删除
        deliveryPickUpStoreMapper.deleteById(id);
    }

    private void validateDeliveryPickUpStoreExists(Long id) {
        if (deliveryPickUpStoreMapper.selectById(id) == null) {
            throw exception(PICK_UP_STORE_NOT_EXISTS);
        }
    }

    @Override
    public DeliveryPickUpStoreDO getDeliveryPickUpStore(Long id) {
        return deliveryPickUpStoreMapper.selectById(id);
    }

    @Override
    public List<DeliveryPickUpStoreDO> getDeliveryPickUpStoreList(Collection<Long> ids) {
        return deliveryPickUpStoreMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<DeliveryPickUpStoreDO> getDeliveryPickUpStorePage(DeliveryPickUpStorePageReqVO pageReqVO) {
        return deliveryPickUpStoreMapper.selectPage(pageReqVO);
    }

    @Override
    public List<DeliveryPickUpStoreDO> getDeliveryPickUpStoreListByStatus(Integer status, List<Long> storeIds) {
        return deliveryPickUpStoreMapper.selectListByStatus(status, storeIds);
    }

    @Override
    public void bindDeliveryPickUpBindStoreStaffId(DeliveryPickUpBindStoreStaffIdReqVO bindStoreStaffIdVO) {
        //查询旧列表
        List<DeliveryPickUpStoreStaffDO> storeStaffDOS = deliveryPickUpStoreStaffMapper.getUserIdsByStoreId(bindStoreStaffIdVO.getId());
        List<Long> oldStoreStaffIds = storeStaffDOS.stream().map(DeliveryPickUpStoreStaffDO::getAdminUserId).toList();
        List<Long> newStoreStaffIds = bindStoreStaffIdVO.getStoreStaffIds();
        List<List<Long>> diffList = CollectionUtils.diffList(oldStoreStaffIds, newStoreStaffIds, // id 不同，就认为是不同的记录
                ObjectUtil::equal);
        // 添加
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(id -> {
                DeliveryPickUpStoreStaffDO storeStaffDO = new DeliveryPickUpStoreStaffDO();
                storeStaffDO.setStoreId(bindStoreStaffIdVO.getId());
                storeStaffDO.setAdminUserId(id);
                deliveryPickUpStoreStaffMapper.insert(storeStaffDO);
            });
        }
        //删除
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            deliveryPickUpStoreStaffMapper.deleteStaffByUserIdsAndStoreId(diffList.get(2), bindStoreStaffIdVO.getId());
        }

    }

}
