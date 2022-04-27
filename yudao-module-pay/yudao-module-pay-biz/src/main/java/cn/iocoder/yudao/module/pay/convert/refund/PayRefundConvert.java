package cn.iocoder.yudao.module.pay.convert.refund;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.*;
import cn.iocoder.yudao.module.pay.controller.app.refund.vo.AppPayRefundReqVO;
import cn.iocoder.yudao.module.pay.controller.app.refund.vo.AppPayRefundRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.service.order.dto.PayRefundReqDTO;
import cn.iocoder.yudao.module.pay.service.order.dto.PayRefundRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 退款订单 Convert
 *
 * @author aquan
 */
@Mapper
public interface PayRefundConvert {

    PayRefundConvert INSTANCE = Mappers.getMapper(PayRefundConvert.class);

    PayRefundDO convert(PayRefundCreateReqVO bean);

    PayRefundDO convert(PayRefundUpdateReqVO bean);

    PayRefundRespVO convert(PayRefundDO bean);

    /**
     * 退款订单 DO 转 退款详情订单 VO
     *
     * @param bean 退款订单 DO
     * @return 退款详情订单 VO
     */
    PayRefundDetailsRespVO refundDetailConvert(PayRefundDO bean);

    /**
     * 退款订单DO 转 分页退款条目VO
     *
     * @param bean 退款订单DO
     * @return 分页退款条目VO
     */
    PayRefundPageItemRespVO pageItemConvert(PayRefundDO bean);

    List<PayRefundRespVO> convertList(List<PayRefundDO> list);

    PageResult<PayRefundRespVO> convertPage(PageResult<PayRefundDO> page);

    List<PayRefundExcelVO> convertList02(List<PayRefundDO> list);

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
        payRefundExcelVO.setTradeNo(bean.getTradeNo());
        payRefundExcelVO.setMerchantOrderId(bean.getMerchantOrderId());
        payRefundExcelVO.setMerchantRefundNo(bean.getMerchantRefundNo());
        payRefundExcelVO.setNotifyUrl(bean.getNotifyUrl());
        payRefundExcelVO.setNotifyStatus(bean.getNotifyStatus());
        payRefundExcelVO.setStatus(bean.getStatus());
        payRefundExcelVO.setType(bean.getType());
        payRefundExcelVO.setReason(bean.getReason());
        payRefundExcelVO.setUserIp(bean.getUserIp());
        payRefundExcelVO.setChannelOrderNo(bean.getChannelOrderNo());
        payRefundExcelVO.setChannelRefundNo(bean.getChannelRefundNo());
        payRefundExcelVO.setExpireTime(bean.getExpireTime());
        payRefundExcelVO.setSuccessTime(bean.getSuccessTime());
        payRefundExcelVO.setNotifyTime(bean.getNotifyTime());
        payRefundExcelVO.setCreateTime(bean.getCreateTime());

        BigDecimal multiple = new BigDecimal(100);
        payRefundExcelVO.setPayAmount(BigDecimal.valueOf(bean.getPayAmount())
                .divide(multiple, 2, RoundingMode.HALF_UP).toString());
        payRefundExcelVO.setRefundAmount(BigDecimal.valueOf(bean.getRefundAmount())
                .divide(multiple, 2, RoundingMode.HALF_UP).toString());

        return payRefundExcelVO;
    }

    //TODO 太多需要处理了， 暂时不用
    @Mappings(value = {
            @Mapping(source = "amount", target = "payAmount"),
            @Mapping(source = "id", target = "orderId"),
            @Mapping(target = "status",ignore = true)
    })
    PayRefundDO convert(PayOrderDO orderDO);

    PayRefundReqDTO convert(AppPayRefundReqVO bean);

    AppPayRefundRespVO convert(PayRefundRespDTO bean);

}
