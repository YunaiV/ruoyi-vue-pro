package cn.iocoder.yudao.module.trade.convert.delivery;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.DeliveryExpressCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.DeliveryExpressExcelVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.DeliveryExpressRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.DeliveryExpressUpdateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


/**
 * 快递公司 Convert
 *
 * @author jason
 */
@Mapper
public interface DeliveryExpressConvert {

    DeliveryExpressConvert INSTANCE = Mappers.getMapper(DeliveryExpressConvert.class);

    DeliveryExpressDO convert(DeliveryExpressCreateReqVO bean);

    DeliveryExpressDO convert(DeliveryExpressUpdateReqVO bean);

    DeliveryExpressRespVO convert(DeliveryExpressDO bean);

    List<DeliveryExpressRespVO> convertList(List<DeliveryExpressDO> list);

    PageResult<DeliveryExpressRespVO> convertPage(PageResult<DeliveryExpressDO> page);

    List<DeliveryExpressExcelVO> convertList02(List<DeliveryExpressDO> list);

}
