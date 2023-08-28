package cn.iocoder.yudao.module.trade.convert.aftersale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import cn.iocoder.yudao.module.product.api.property.dto.ProductPropertyValueDetailRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSaleDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSaleRespPageItemVO;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.log.TradeAfterSaleLogRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.base.member.user.MemberUserRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.base.product.property.ProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderBaseVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleLogDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.framework.aftersalelog.core.dto.TradeAfterSaleLogRespDTO;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

@Mapper
public interface TradeAfterSaleConvert {

    TradeAfterSaleConvert INSTANCE = Mappers.getMapper(TradeAfterSaleConvert.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "creator", ignore = true),
            @Mapping(target = "updater", ignore = true),
    })
    TradeAfterSaleDO convert(AppTradeAfterSaleCreateReqVO createReqVO, TradeOrderItemDO tradeOrderItem);

    @Mappings({
            @Mapping(source = "afterSale.orderId", target = "merchantOrderId"),
            @Mapping(source = "afterSale.id", target = "merchantRefundId"),
            @Mapping(source = "afterSale.applyReason", target = "reason"),
            @Mapping(source = "afterSale.refundPrice", target = "price")
    })
    PayRefundCreateReqDTO convert(String userIp, TradeAfterSaleDO afterSale,
                                  TradeOrderProperties orderProperties);

    MemberUserRespVO convert(MemberUserRespDTO bean);

    PageResult<TradeAfterSaleRespPageItemVO> convertPage(PageResult<TradeAfterSaleDO> page);

    default PageResult<TradeAfterSaleRespPageItemVO> convertPage(PageResult<TradeAfterSaleDO> pageResult,
                                                                 Map<Long, MemberUserRespDTO> memberUsers) {
        PageResult<TradeAfterSaleRespPageItemVO> voPageResult = convertPage(pageResult);
        // 处理会员
        voPageResult.getList().forEach(afterSale -> afterSale.setUser(
                convert(memberUsers.get(afterSale.getUserId()))));
        return voPageResult;
    }

    ProductPropertyValueDetailRespVO convert(ProductPropertyValueDetailRespDTO bean);

    AppTradeAfterSaleRespVO convert(TradeAfterSaleDO bean);

    PageResult<AppTradeAfterSaleRespVO> convertPage02(PageResult<TradeAfterSaleDO> page);

    List<TradeAfterSaleLogRespDTO> convertList(List<TradeAfterSaleLogDO> list);

    List<TradeAfterSaleLogRespVO> convertList1(List<TradeAfterSaleLogRespDTO> list);

    TradeOrderBaseVO convert(TradeOrderDO order);

    TradeAfterSaleDetailRespVO convert(TradeAfterSaleDO afterSale, List<TradeOrderItemDO> orderItems, List<TradeAfterSaleLogRespVO> logs);

    default TradeAfterSaleDetailRespVO convert(TradeAfterSaleDO afterSale, TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                               MemberUserRespDTO user, List<TradeAfterSaleLogRespDTO> logs) {
        TradeAfterSaleDetailRespVO respVO = convert(afterSale, orderItems, convertList1(logs));
        // 处理用户信息
        respVO.setUser(convert(user));
        // 处理订单信息
        respVO.setOrder(convert(order));
        return respVO;
    }
}
