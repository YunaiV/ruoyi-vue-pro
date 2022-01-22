package cn.iocoder.yudao.adminserver.modules.pay.convert.order;

import cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.order.PayOrderDetailsRespVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.order.PayOrderExcelVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.order.PayOrderPageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.order.PayOrderRespVO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.mapstruct.Mapper;
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

    /**
     * 订单DO 转换为 详细订单 RespVO
     *
     * @param bean 订单DO
     * @return 详细订单 RespVO
     */
    PayOrderDetailsRespVO orderDetailConvert(PayOrderDO bean);

    /**
     * 订单扩展DO 转换为 详细订单扩展 RespVO
     *
     * @param bean 订单扩展DO
     * @return 详细订单扩展 RespVO
     */
    PayOrderDetailsRespVO.PayOrderExtension orderDetailExtensionConvert(PayOrderExtensionDO bean);

    List<PayOrderRespVO> convertList(List<PayOrderDO> list);

    PageResult<PayOrderRespVO> convertPage(PageResult<PayOrderDO> page);

    List<PayOrderExcelVO> convertList02(List<PayOrderDO> list);

    /**
     * 订单DO转自定义分页对象
     *
     * @param bean 订单DO
     * @return 分页对象
     */
    PayOrderPageItemRespVO pageConvertItemPage(PayOrderDO bean);

    /**
     * 订单DO 转 订单导出excel VO
     *
     * @param bean 订单 DO
     * @return 订单导出excel VO
     */
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
}
