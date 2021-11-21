package cn.iocoder.yudao.userserver.modules.pay.convert.order;

import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundReqBO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundRespBO;
import cn.iocoder.yudao.userserver.modules.pay.controller.order.vo.PayRefundReqVO;
import cn.iocoder.yudao.userserver.modules.pay.controller.order.vo.PayRefundRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 支付退款 Convert
 * @author jason
 */
@Mapper
public interface PayRefundConvert {

    PayRefundConvert INSTANCE = Mappers.getMapper(PayRefundConvert.class);

    PayRefundReqBO convert(PayRefundReqVO reqVO);

    PayRefundRespVO convert(PayRefundRespBO respBO);
}
