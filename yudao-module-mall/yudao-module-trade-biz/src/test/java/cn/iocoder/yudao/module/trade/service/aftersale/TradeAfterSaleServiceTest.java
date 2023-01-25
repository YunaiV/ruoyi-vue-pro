package cn.iocoder.yudao.module.trade.service.aftersale;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSalePageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleLogDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.aftersale.TradeAfterSaleLogMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.aftersale.TradeAfterSaleMapper;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleTypeEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleWayEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
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
    @Resource
    private TradeAfterSaleLogMapper tradeAfterSaleLogMapper;

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
                .setOrderItemId(1L).setRefundPrice(100).setWay(TradeAfterSaleWayEnum.RETURN_AND_REFUND.getWay())
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
        TradeOrderDO order = randomPojo(TradeOrderDO.class, o -> o.setStatus(TradeOrderStatusEnum.DELIVERED.getStatus())
                .setNo("202211301234"));
        when(tradeOrderService.getOrder(eq(1024L), eq(111L))).thenReturn(order);

        // 调用
        Long afterSaleId = tradeAfterSaleService.createAfterSale(userId, createReqVO);
        // 断言（TradeAfterSaleDO）
        TradeAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(afterSaleId);
        assertNotNull(afterSale.getNo());
        assertEquals(afterSale.getStatus(), TradeAfterSaleStatusEnum.APPLY.getStatus());
        assertEquals(afterSale.getType(), TradeAfterSaleTypeEnum.IN_SALE.getType());
        assertPojoEquals(afterSale, createReqVO);
        assertEquals(afterSale.getUserId(), 1024L);
        assertPojoEquals(afterSale, orderItem, "id", "creator", "createTime", "updater", "updateTime");
        assertEquals(afterSale.getOrderNo(), "202211301234");
        assertNull(afterSale.getPayRefundId());
        assertNull(afterSale.getRefundTime());
        assertNull(afterSale.getLogisticsId());
        assertNull(afterSale.getLogisticsNo());
        assertNull(afterSale.getDeliveryTime());
        assertNull(afterSale.getReceiveReason());
        // 断言（TradeAfterSaleLogDO）
        TradeAfterSaleLogDO afterSaleLog = tradeAfterSaleLogMapper.selectList().get(0);
        assertEquals(afterSaleLog.getUserId(), userId);
        assertEquals(afterSaleLog.getUserType(), UserTypeEnum.MEMBER.getValue());
        assertEquals(afterSaleLog.getAfterSaleId(), afterSaleId);
        assertPojoEquals(afterSale, orderItem, "id", "creator", "createTime", "updater", "updateTime");
        assertNull(afterSaleLog.getBeforeStatus());
        assertEquals(afterSaleLog.getAfterStatus(), TradeAfterSaleStatusEnum.APPLY.getStatus());
        assertEquals(afterSaleLog.getContent(), TradeAfterSaleStatusEnum.APPLY.getContent());
    }

    @Test
    public void testGetAfterSalePage() {
        // mock 数据
        TradeAfterSaleDO dbAfterSale = randomPojo(TradeAfterSaleDO.class, o -> { // 等会查询到
            o.setNo("202211190847450020500077");
            o.setStatus(TradeAfterSaleStatusEnum.APPLY.getStatus());
            o.setWay(TradeAfterSaleWayEnum.RETURN_AND_REFUND.getWay());
            o.setType(TradeAfterSaleTypeEnum.IN_SALE.getType());
            o.setOrderNo("202211190847450020500011");
            o.setSpuName("芋艿");
            o.setCreateTime(buildTime(2022, 1, 15));
        });
        tradeAfterSaleMapper.insert(dbAfterSale);
        // 测试 no 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setNo("202211190847450020500066")));
        // 测试 status 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setStatus(TradeAfterSaleStatusEnum.SELLER_REFUSE.getStatus())));
        // 测试 way 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setWay(TradeAfterSaleWayEnum.REFUND.getWay())));
        // 测试 type 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setType(TradeAfterSaleTypeEnum.AFTER_SALE.getType())));
        // 测试 orderNo 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setOrderNo("202211190847450020500022")));
        // 测试 spuName 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setSpuName("土豆")));
        // 测试 createTime 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setCreateTime(buildTime(2022, 1, 20))));
        // 准备参数
        TradeAfterSalePageReqVO reqVO = new TradeAfterSalePageReqVO();
        reqVO.setNo("20221119084745002050007");
        reqVO.setStatus(TradeAfterSaleStatusEnum.APPLY.getStatus());
        reqVO.setWay(TradeAfterSaleWayEnum.RETURN_AND_REFUND.getWay());
        reqVO.setType(TradeAfterSaleTypeEnum.IN_SALE.getType());
        reqVO.setOrderNo("20221119084745002050001");
        reqVO.setSpuName("芋");
        reqVO.setCreateTime(new LocalDateTime[]{buildTime(2022, 1, 1), buildTime(2022, 1, 16)});

        // 调用
        PageResult<TradeAfterSaleDO> pageResult = tradeAfterSaleService.getAfterSalePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbAfterSale, pageResult.getList().get(0));
    }
}
