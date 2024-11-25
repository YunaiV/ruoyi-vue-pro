package cn.iocoder.yudao.module.trade.service.delivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpBindReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStoreCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStorePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStoreUpdateReqVO;
import cn.iocoder.yudao.module.trade.convert.delivery.DeliveryPickUpStoreConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreDO;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryPickUpStoreMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

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
    private AdminUserApi adminUserApi;

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
        DeliveryPickUpStoreDO deliveryPickUpStore = deliveryPickUpStoreMapper.selectById(id);
        if (deliveryPickUpStore == null) {
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
    public List<DeliveryPickUpStoreDO> getDeliveryPickUpStoreListByStatus(Integer status) {
        return deliveryPickUpStoreMapper.selectListByStatus(status);
    }

    @Override
    public PageResult<DeliveryPickUpStoreDO> getDeliveryPickUpStorePage(DeliveryPickUpStorePageReqVO pageReqVO) {
        return deliveryPickUpStoreMapper.selectPage(pageReqVO);
    }

    @Override
    public void bindDeliveryPickUpStore(DeliveryPickUpBindReqVO bindReqVO) {
        // 1.1 校验门店存在
        validateDeliveryPickUpStoreExists(bindReqVO.getId());
        // 1.2 校验用户存在
        adminUserApi.validateUserList(bindReqVO.getVerifyUserIds());

        // 2. 更新
        DeliveryPickUpStoreDO updateObj = BeanUtils.toBean(bindReqVO, DeliveryPickUpStoreDO.class);
        deliveryPickUpStoreMapper.updateById(updateObj);
    }

}
