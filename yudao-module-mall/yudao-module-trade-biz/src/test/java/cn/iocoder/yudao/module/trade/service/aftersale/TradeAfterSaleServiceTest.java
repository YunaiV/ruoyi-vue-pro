package cn.iocoder.yudao.module.trade.service.aftersale;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.aftersale.TradeAfterSaleMapper;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link TradeAfterSaleService} 的单元测试
 *
 * @author 芋道源码
 */
@Import(TradeAfterSaleServiceImpl.class)
public class TradeAfterSaleServiceTest extends BaseDbUnitTest {

    @Resource
    private TradeAfterSaleServiceImpl tradeAfterSaleService;

    @Resource
    private TradeAfterSaleMapper tradeAfterSaleMapper;

    @MockBean
    private TradeOrderService tradeOrderService;
    @MockBean
    private PayRefundApi payRefundApi;

    @MockBean
    private TradeOrderProperties tradeOrderProperties;

    @Test
    public void testCreateAfterSale() {
        // 准备参数
        Long userId = 1024L;
        AppTradeAfterSaleCreateReqVO createReqVO = new AppTradeAfterSaleCreateReqVO()
                .setOrderItemId(1L).setRefundPrice(100).setType(TradeAfterSaleTypeEnum.RETURN_AND_REFUND.getType())
                .setApplyReason("退钱").setApplyDescription("快退")
                .setApplyPicUrls(asList("https://www.baidu.com/1.png", "https://www.baidu.com/2.png"));
        // mock 方法（交易订单项）
        TradeOrderItemDO orderItem = randomPojo(TradeOrderItemDO.class, o -> {
            o.setOrderId(111L).setUserId(userId).setOrderDividePrice(200);
            o.setAfterSaleStatus(TradeOrderItemAfterSaleStatusEnum.NONE.getStatus());
        });
        when(tradeOrderService.getOrderItem(eq(1024L), eq(1L)))
                .thenReturn(orderItem);
        // mock 方法（交易订单）
        TradeOrderDO order = randomPojo(TradeOrderDO.class, o -> o.setStatus(TradeOrderStatusEnum.DELIVERED.getStatus()));
        when(tradeOrderService.getOrder(eq(1024L), eq(111L))).thenReturn(order);

        // 调用
        Long afterSaleId = tradeAfterSaleService.createAfterSale(userId, createReqVO);
        // 断言
        TradeAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(afterSaleId);
        assertNotNull(afterSale.getNo());
        assertEquals(afterSale.getStatus(), TradeAfterSaleStatusEnum.APPLY.getStatus());
        assertPojoEquals(afterSale, createReqVO);
        assertEquals(afterSale.getUserId(), 1024L);
        assertPojoEquals(afterSale, orderItem, "id", "creator", "createTime", "updater", "updateTime");
        assertNull(afterSale.getPayRefundId());
        assertNull(afterSale.getRefundTime());
        assertNull(afterSale.getLogisticsId());
        assertNull(afterSale.getLogisticsNo());
        assertNull(afterSale.getDeliveryTime());
        assertNull(afterSale.getReceiveReason());
    }

}
