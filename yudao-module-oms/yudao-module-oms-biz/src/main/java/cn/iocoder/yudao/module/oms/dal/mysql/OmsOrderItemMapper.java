package cn.iocoder.yudao.module.oms.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oms.controller.admin.order.item.vo.OmsOrderItemPageReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * OMS订单项 Mapper
 *
 * @author 谷毛毛
 */
@Mapper
public interface OmsOrderItemMapper extends BaseMapperX<OmsOrderItemDO> {

    default PageResult<OmsOrderItemDO> selectPage(PageParam reqVO, Long orderId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OmsOrderItemDO>()
            .eq(OmsOrderItemDO::getOrderId, orderId)
            .orderByDesc(OmsOrderItemDO::getId));
    }

    default PageResult<OmsOrderItemDO> selectPage(OmsOrderItemPageReqVO pageReqVO) {
        return null;
    }

    default int deleteByOrderId(Long orderId) {
        return delete(OmsOrderItemDO::getOrderId, orderId);
    }

    default int deleteByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return 0;
        }
        return delete(new LambdaQueryWrapperX<OmsOrderItemDO>()
            .in(OmsOrderItemDO::getOrderId, orderIds));
    }


    default List<OmsOrderItemDO> selectListByOrderIds(Collection<Long> orderIds) {
        return selectList(OmsOrderItemDO::getOrderId, orderIds);
    }
}
