package cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayRefundDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;


/**
 * 退款订单 Mapper
 *
 */
@Mapper
public interface PayRefundCoreMapper extends BaseMapperX<PayRefundDO> {

    default PayRefundDO selectByReqNo(String reqNo) {
        return selectOne("req_no", reqNo);
    }

    default  PayRefundDO selectByTradeNoAndMerchantRefundNo(String tradeNo, String merchantRefundNo){
        return selectOne("trade_no", tradeNo, "merchant_refund_no", merchantRefundNo);
    }

}
