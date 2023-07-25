package cn.iocoder.yudao.module.trade.convert.delivery;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStoreCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStoreExcelVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStoreRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.DeliveryPickUpStoreUpdateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DeliveryPickUpStoreConvert {

    DeliveryPickUpStoreConvert INSTANCE = Mappers.getMapper(DeliveryPickUpStoreConvert.class);

    DeliveryPickUpStoreDO convert(DeliveryPickUpStoreCreateReqVO bean);

    DeliveryPickUpStoreDO convert(DeliveryPickUpStoreUpdateReqVO bean);

    DeliveryPickUpStoreRespVO convert(DeliveryPickUpStoreDO bean);

    List<DeliveryPickUpStoreRespVO> convertList(List<DeliveryPickUpStoreDO> list);

    PageResult<DeliveryPickUpStoreRespVO> convertPage(PageResult<DeliveryPickUpStoreDO> page);

    List<DeliveryPickUpStoreExcelVO> convertList02(List<DeliveryPickUpStoreDO> list);

    @Mapping(source = "areaId", target = "areaName",  qualifiedByName = "convertAreaIdToName")
    DeliveryPickUpStoreExcelVO convert2(DeliveryPickUpStoreDO bean);

    @Named("convertAreaIdToName")
    default String convertAreaIdToName(Integer areaId) {
        return AreaUtils.format(areaId);
    }

}
