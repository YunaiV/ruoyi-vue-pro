package cn.iocoder.yudao.module.pay.convert.refund;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundDetailsRespVO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundExcelVO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundPageItemRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Mapper
public interface PayRefundConvert {

    PayRefundConvert INSTANCE = Mappers.getMapper(PayRefundConvert.class);


    default PayRefundDetailsRespVO convert(PayRefundDO refund, PayOrderDO order, PayAppDO app) {
        PayRefundDetailsRespVO respVO = convert(refund)
                .setOrder(convert(order));
        if (app != null) {
            respVO.setAppName(app.getName());
        }
        return respVO;
    }
    PayRefundDetailsRespVO convert(PayRefundDO bean);
    PayRefundDetailsRespVO.Order convert(PayOrderDO bean);

    default PageResult<PayRefundPageItemRespVO> convertPage(PageResult<PayRefundDO> page, Map<Long, PayAppDO> appMap) {
        PageResult<PayRefundPageItemRespVO> result = convertPage(page);
        result.getList().forEach(order -> MapUtils.findAndThen(appMap, order.getAppId(), app -> order.setAppName(app.getName())));
        return result;
    }
    PageResult<PayRefundPageItemRespVO> convertPage(PageResult<PayRefundDO> page);

    /**
     * 退款订单DO 转 导出excel VO
     *
     * @param bean 退款订单DO
     * @return 导出 excel VO
     */
    default PayRefundExcelVO excelConvert(PayRefundDO bean) {
        if (bean == null) {
            return null;
        }

        PayRefundExcelVO payRefundExcelVO = new PayRefundExcelVO();

        payRefundExcelVO.setId(bean.getId());
        payRefundExcelVO.setTradeNo(bean.getNo());
        payRefundExcelVO.setMerchantOrderId(bean.getMerchantOrderId());
        // TODO 芋艿：晚点在改
//        payRefundExcelVO.setMerchantRefundNo(bean.getMerchantRefundNo());
        payRefundExcelVO.setNotifyUrl(bean.getNotifyUrl());
        payRefundExcelVO.setStatus(bean.getStatus());
        payRefundExcelVO.setReason(bean.getReason());
        payRefundExcelVO.setUserIp(bean.getUserIp());
        payRefundExcelVO.setChannelOrderNo(bean.getChannelOrderNo());
        payRefundExcelVO.setChannelRefundNo(bean.getChannelRefundNo());
        payRefundExcelVO.setSuccessTime(bean.getSuccessTime());
        payRefundExcelVO.setCreateTime(bean.getCreateTime());

        BigDecimal multiple = new BigDecimal(100);
        payRefundExcelVO.setPayPrice(BigDecimal.valueOf(bean.getPayPrice())
                .divide(multiple, 2, RoundingMode.HALF_UP).toString());
        payRefundExcelVO.setRefundPrice(BigDecimal.valueOf(bean.getRefundPrice())
                .divide(multiple, 2, RoundingMode.HALF_UP).toString());

        return payRefundExcelVO;
    }

    PayRefundDO convert(PayRefundCreateReqDTO bean);

    PayRefundRespDTO convert02(PayRefundDO bean);

}
