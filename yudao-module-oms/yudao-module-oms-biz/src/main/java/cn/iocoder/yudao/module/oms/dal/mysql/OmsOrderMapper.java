package cn.iocoder.yudao.module.oms.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.oms.controller.admin.order.vo.OmsOrderPageReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * OMS订单 Mapper
 *
 * @author 谷毛毛
 */
@Mapper
public interface OmsOrderMapper extends BaseMapperX<OmsOrderDO> {

    default PageResult<OmsOrderDO> selectPage(OmsOrderPageReqVO reqVO) {
        return selectPage(reqVO, bindQueryWrapper(reqVO));
    }

    default MPJLambdaWrapperX<OmsOrderDO> bindQueryWrapper(OmsOrderPageReqVO reqVO) {
        return new MPJLambdaWrapperX<OmsOrderDO>()
            .eqIfPresent(OmsOrderDO::getPlatformCode, reqVO.getPlatformCode())
            .eqIfPresent(OmsOrderDO::getCode, reqVO.getCode())
            .eqIfPresent(OmsOrderDO::getExternalCode, reqVO.getExternalCode())
            .eqIfPresent(OmsOrderDO::getShopId, reqVO.getShopId())
            .eqIfPresent(OmsOrderDO::getShippingFee, reqVO.getShippingFee())
            .eqIfPresent(OmsOrderDO::getTotalPrice, reqVO.getTotalPrice())
            .likeIfPresent(OmsOrderDO::getBuyerName, reqVO.getBuyerName())
            .eqIfPresent(OmsOrderDO::getEmail, reqVO.getEmail())
            .betweenIfPresent(OmsOrderDO::getReceiveLatestTime, reqVO.getReceiveLatestTime())
            .betweenIfPresent(OmsOrderDO::getOrderCreateTime, reqVO.getOrderCreateTime())
            .betweenIfPresent(OmsOrderDO::getPayTime, reqVO.getPayTime())
            .eqIfPresent(OmsOrderDO::getPhone, reqVO.getPhone())
            .likeIfPresent(OmsOrderDO::getCompanyName, reqVO.getCompanyName())
            .eqIfPresent(OmsOrderDO::getRecipientCountryCode, reqVO.getRecipientCountryCode())
            .eqIfPresent(OmsOrderDO::getState, reqVO.getState())
            .eqIfPresent(OmsOrderDO::getCity, reqVO.getCity())
            .eqIfPresent(OmsOrderDO::getDistrict, reqVO.getDistrict())
            .eqIfPresent(OmsOrderDO::getExternalAddress, reqVO.getExternalAddress())
            .eqIfPresent(OmsOrderDO::getAddress1, reqVO.getAddress1())
            .eqIfPresent(OmsOrderDO::getHouseNo, reqVO.getHouseNo())
            .eqIfPresent(OmsOrderDO::getPostalCode, reqVO.getPostalCode())
            .betweenIfPresent(OmsOrderDO::getCreateTime, reqVO.getCreateTime())
            .orderByDesc(OmsOrderDO::getId);
    }

    default List<OmsOrderDO> getByPlatformCode(String platformCode) {
        return selectList(bindQueryWrapper(OmsOrderPageReqVO.builder().platformCode(platformCode).build()));
    }


}