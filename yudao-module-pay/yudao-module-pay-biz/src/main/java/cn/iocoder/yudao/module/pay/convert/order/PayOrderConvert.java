package cn.iocoder.yudao.module.pay.convert.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderDetailsRespVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderExcelVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderPageItemRespVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.service.order.dto.PayOrderSubmitReqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 支付订单 Convert
 *
 * @author aquan
 */
@Mapper
public interface PayOrderConvert {

    PayOrderConvert INSTANCE = Mappers.getMapper(PayOrderConvert.class);

    PayOrderRespVO convert(PayOrderDO bean);

    PayOrderDetailsRespVO orderDetailConvert(PayOrderDO bean);

    PayOrderDetailsRespVO.PayOrderExtension orderDetailExtensionConvert(PayOrderExtensionDO bean);

    List<PayOrderRespVO> convertList(List<PayOrderDO> list);

    PageResult<PayOrderRespVO> convertPage(PageResult<PayOrderDO> page);

    List<PayOrderExcelVO> convertList02(List<PayOrderDO> list);

    /**
     * 订单 DO 转自定义分页对象
     *
     * @param bean 订单DO
     * @return 分页对象
     */
    PayOrderPageItemRespVO pageConvertItemPage(PayOrderDO bean);

    // TODO 芋艿：优化下 convert 逻辑
    default PayOrderExcelVO excelConvert(PayOrderDO bean) {
        if (bean == null) {
            return null;
        }

        PayOrderExcelVO payOrderExcelVO = new PayOrderExcelVO();

        payOrderExcelVO.setId(bean.getId());
        payOrderExcelVO.setSubject(bean.getSubject());
        payOrderExcelVO.setMerchantOrderId(bean.getMerchantOrderId());
        payOrderExcelVO.setChannelOrderNo(bean.getChannelOrderNo());
        payOrderExcelVO.setStatus(bean.getStatus());
        payOrderExcelVO.setNotifyStatus(bean.getNotifyStatus());
        payOrderExcelVO.setNotifyUrl(bean.getNotifyUrl());
        payOrderExcelVO.setCreateTime(bean.getCreateTime());
        payOrderExcelVO.setSuccessTime(bean.getSuccessTime());
        payOrderExcelVO.setExpireTime(bean.getExpireTime());
        payOrderExcelVO.setNotifyTime(bean.getNotifyTime());
        payOrderExcelVO.setUserIp(bean.getUserIp());
        payOrderExcelVO.setRefundStatus(bean.getRefundStatus());
        payOrderExcelVO.setRefundTimes(bean.getRefundTimes());
        payOrderExcelVO.setBody(bean.getBody());

        BigDecimal multiple = new BigDecimal(100);

        payOrderExcelVO.setAmount(BigDecimal.valueOf(bean.getAmount())
                .divide(multiple, 2, RoundingMode.HALF_UP).toString());

        payOrderExcelVO.setChannelFeeAmount(BigDecimal.valueOf(bean.getChannelFeeAmount())
                .divide(multiple, 2, RoundingMode.HALF_UP).toString());
        payOrderExcelVO.setChannelFeeRate(java.math.BigDecimal.valueOf(bean.getChannelFeeRate())
                .multiply(multiple).toString());
        payOrderExcelVO.setRefundAmount(BigDecimal.valueOf(bean.getRefundAmount())
                .divide(multiple, 2, RoundingMode.HALF_UP).toString());

        return payOrderExcelVO;
    }


    PayOrderDO convert(PayOrderCreateReqDTO bean);

    @Mapping(target = "id", ignore = true)
    PayOrderExtensionDO convert(PayOrderSubmitReqDTO bean);

    PayOrderUnifiedReqDTO convert2(PayOrderSubmitReqDTO bean);

    PayOrderRespDTO convert2(PayOrderDO bean);

}
