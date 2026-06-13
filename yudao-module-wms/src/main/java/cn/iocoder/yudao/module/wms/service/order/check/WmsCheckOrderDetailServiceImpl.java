package cn.iocoder.yudao.module.wms.service.order.check;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.order.WmsCheckOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.check.WmsCheckOrderDetailDO;
import cn.iocoder.yudao.module.wms.dal.mysql.order.check.WmsCheckOrderDetailMapper;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS 盘库单明细 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WmsCheckOrderDetailServiceImpl implements WmsCheckOrderDetailService {

    @Resource
    private WmsCheckOrderDetailMapper checkOrderDetailMapper;
    @Resource
    private WmsItemSkuService itemSkuService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCheckOrderDetailList(Long orderId, WmsCheckOrderSaveReqVO reqVO) {
        List<WmsCheckOrderDetailDO> list = buildCheckOrderDetailList(reqVO);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        list.forEach(detail -> detail.setId(null).setOrderId(orderId));
        checkOrderDetailMapper.insertBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCheckOrderDetailList(Long orderId, WmsCheckOrderSaveReqVO reqVO) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<WmsCheckOrderDetailDO> oldList = checkOrderDetailMapper.selectListByOrderId(orderId);
        List<WmsCheckOrderDetailDO> list = buildCheckOrderDetailList(reqVO);
        List<WmsCheckOrderDetailDO> newList = CollUtil.isEmpty(list) ? ListUtil.of() : list;
        List<List<WmsCheckOrderDetailDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> ObjectUtil.equal(oldVal.getId(), newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            if (CollUtil.isNotEmpty(convertList(diffList.get(0), WmsCheckOrderDetailDO::getId))) {
                throw exception(CHECK_ORDER_DETAIL_NOT_EXISTS);
            }
            diffList.get(0).forEach(detail -> detail.setOrderId(orderId));
            checkOrderDetailMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            diffList.get(1).forEach(detail -> detail.setOrderId(orderId));
            checkOrderDetailMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            checkOrderDetailMapper.deleteByIds(convertList(diffList.get(2), WmsCheckOrderDetailDO::getId));
        }
    }

    @Override
    public void deleteCheckOrderDetailListByOrderId(Long orderId) {
        checkOrderDetailMapper.deleteByOrderId(orderId);
    }

    @Override
    public List<WmsCheckOrderDetailDO> getCheckOrderDetailList(Long orderId) {
        return checkOrderDetailMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<WmsCheckOrderDetailDO> getCheckOrderDetailList(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return ListUtil.of();
        }
        return checkOrderDetailMapper.selectListByOrderIds(orderIds);
    }

    @Override
    public List<WmsCheckOrderDetailDO> validateCheckOrderDetailListExists(Long orderId) {
        List<WmsCheckOrderDetailDO> details = checkOrderDetailMapper.selectListByOrderId(orderId);
        if (CollUtil.isEmpty(details)) {
            throw exception(CHECK_ORDER_DETAIL_REQUIRED);
        }
        return details;
    }

    @Override
    public long getCheckOrderDetailCountBySkuId(Long skuId) {
        return checkOrderDetailMapper.selectCountBySkuId(skuId);
    }

    private List<WmsCheckOrderDetailDO> buildCheckOrderDetailList(WmsCheckOrderSaveReqVO reqVO) {
        if (CollUtil.isEmpty(reqVO.getDetails())) {
            return ListUtil.of();
        }
        return convertList(reqVO.getDetails(), detail -> {
            // 校验 SKU 存在
            itemSkuService.validateItemSkuExists(detail.getSkuId());
            // 构建对象
            return BeanUtils.toBean(detail, WmsCheckOrderDetailDO.class).setWarehouseId(reqVO.getWarehouseId());
        });
    }

}
