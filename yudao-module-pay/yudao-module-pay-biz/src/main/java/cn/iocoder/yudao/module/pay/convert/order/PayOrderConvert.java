package cn.iocoder.yudao.module.pay.convert.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.pay.controller.app.order.vo.AppPayOrderSubmitRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 支付订单 Convert
 *
 * @author aquan
 */
@Mapper
public interface PayOrderConvert {

    PayOrderConvert INSTANCE = Mappers.getMapper(PayOrderConvert.class);

    PayOrderRespVO convert(PayOrderDO bean);

    PayOrderRespDTO convert2(PayOrderDO order);

    default PayOrderDetailsRespVO convert(PayOrderDO order, PayOrderExtensionDO orderExtension, PayAppDO app) {
        PayOrderDetailsRespVO respVO = convertDetail(order);
        respVO.setExtension(convert(orderExtension));
        if (app != null) {
            respVO.setAppName(app.getName());
        }
        return respVO;
    }
    PayOrderDetailsRespVO convertDetail(PayOrderDO bean);
    PayOrderDetailsRespVO.PayOrderExtension convert(PayOrderExtensionDO bean);

    default PageResult<PayOrderPageItemRespVO> convertPage(PageResult<PayOrderDO> page, Map<Long, PayAppDO> appMap) {
        PageResult<PayOrderPageItemRespVO> result = convertPage(page);
        result.getList().forEach(order -> MapUtils.findAndThen(appMap, order.getAppId(), app -> order.setAppName(app.getName())));
        return result;
    }
    PageResult<PayOrderPageItemRespVO> convertPage(PageResult<PayOrderDO> page);

    default List<PayOrderExcelVO> convertList(List<PayOrderDO> list, Map<Long, PayAppDO> appMap) {
        return CollectionUtils.convertList(list, order -> {
            PayOrderExcelVO excelVO = convertExcel(order);
            MapUtils.findAndThen(appMap, order.getAppId(), app -> excelVO.setAppName(app.getName()));
            return excelVO;
        });
    }
    PayOrderExcelVO convertExcel(PayOrderDO bean);

    PayOrderDO convert(PayOrderCreateReqDTO bean);

    @Mapping(target = "id", ignore = true)
    PayOrderExtensionDO convert(PayOrderSubmitReqVO bean, String userIp);

    PayOrderUnifiedReqDTO convert2(PayOrderSubmitReqVO reqVO, String userIp);

    @Mapping(source = "order.status", target = "status")
    PayOrderSubmitRespVO convert(PayOrderDO order, cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO respDTO);

    AppPayOrderSubmitRespVO convert3(PayOrderSubmitRespVO bean);

}
