package cn.iocoder.yudao.module.trade.service.aftersale;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.AfterSalePageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppAfterSaleCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleLogDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.aftersale.AfterSaleLogMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.aftersale.AfterSaleMapper;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleTypeEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleWayEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderUpdateService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
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
 * {@link AfterSaleService} 的单元测试
 *
 * @author 芋道源码
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(AfterSaleServiceImpl.class)
public class AfterSaleServiceTest extends BaseDbUnitTest {

    @Resource
    private AfterSaleServiceImpl tradeAfterSaleService;

    @Resource
    private AfterSaleMapper tradeAfterSaleMapper;
    @Resource
    private AfterSaleLogMapper tradeAfterSaleLogMapper;

    @MockBean
    private TradeOrderUpdateService tradeOrderUpdateService;
    @Resource
    private TradeOrderQueryService tradeOrderQueryService;

    @MockBean
    private PayRefundApi payRefundApi;

    @MockBean
    private TradeOrderProperties tradeOrderProperties;

    @Test
    public void testCreateAfterSale() {
        // 准备参数
        Long userId = 1024L;
        AppAfterSaleCreateReqVO createReqVO = new AppAfterSaleCreateReqVO()
                .setOrderItemId(1L).setRefundPrice(100).setWay(AfterSaleWayEnum.RETURN_AND_REFUND.getWay())
                .setApplyReason("退钱").setApplyDescription("快退")
                .setApplyPicUrls(asList("https://www.baidu.com/1.png", "https://www.baidu.com/2.png"));
        // mock 方法（交易订单项）
        TradeOrderItemDO orderItem = randomPojo(TradeOrderItemDO.class, o -> {
            o.setOrderId(111L).setUserId(userId).setPayPrice(200);
            o.setAfterSaleStatus(TradeOrderItemAfterSaleStatusEnum.NONE.getStatus());
        });
        when(tradeOrderQueryService.getOrderItem(eq(1024L), eq(1L)))
                .thenReturn(orderItem);
        // mock 方法（交易订单）
        TradeOrderDO order = randomPojo(TradeOrderDO.class, o -> o.setStatus(TradeOrderStatusEnum.DELIVERED.getStatus())
                .setNo("202211301234"));
        when(tradeOrderQueryService.getOrder(eq(1024L), eq(111L))).thenReturn(order);

        // 调用
        Long afterSaleId = tradeAfterSaleService.createAfterSale(userId, createReqVO);
        // 断言（TradeAfterSaleDO）
        AfterSaleDO afterSale = tradeAfterSaleMapper.selectById(afterSaleId);
        assertNotNull(afterSale.getNo());
        assertEquals(afterSale.getStatus(), AfterSaleStatusEnum.APPLY.getStatus());
        assertEquals(afterSale.getType(), AfterSaleTypeEnum.IN_SALE.getType());
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
        AfterSaleLogDO afterSaleLog = tradeAfterSaleLogMapper.selectList().get(0);
        assertEquals(afterSaleLog.getUserId(), userId);
        assertEquals(afterSaleLog.getUserType(), UserTypeEnum.MEMBER.getValue());
        assertEquals(afterSaleLog.getAfterSaleId(), afterSaleId);
        assertPojoEquals(afterSale, orderItem, "id", "creator", "createTime", "updater", "updateTime");
        assertEquals(afterSaleLog.getContent(), AfterSaleStatusEnum.APPLY.getContent());
    }

    @Test
    public void testGetAfterSalePage() {
        // mock 数据
        AfterSaleDO dbAfterSale = randomPojo(AfterSaleDO.class, o -> { // 等会查询到
            o.setNo("202211190847450020500077");
            o.setStatus(AfterSaleStatusEnum.APPLY.getStatus());
            o.setWay(AfterSaleWayEnum.RETURN_AND_REFUND.getWay());
            o.setType(AfterSaleTypeEnum.IN_SALE.getType());
            o.setOrderNo("202211190847450020500011");
            o.setSpuName("芋艿");
            o.setCreateTime(buildTime(2022, 1, 15));
        });
        tradeAfterSaleMapper.insert(dbAfterSale);
        // 测试 no 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setNo("202211190847450020500066")));
        // 测试 status 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setStatus(AfterSaleStatusEnum.SELLER_REFUSE.getStatus())));
        // 测试 way 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setWay(AfterSaleWayEnum.REFUND.getWay())));
        // 测试 type 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setType(AfterSaleTypeEnum.AFTER_SALE.getType())));
        // 测试 orderNo 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setOrderNo("202211190847450020500022")));
        // 测试 spuName 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setSpuName("土豆")));
        // 测试 createTime 不匹配
        tradeAfterSaleMapper.insert(cloneIgnoreId(dbAfterSale, o -> o.setCreateTime(buildTime(2022, 1, 20))));
        // 准备参数
        AfterSalePageReqVO reqVO = new AfterSalePageReqVO();
        reqVO.setNo("20221119084745002050007");
        reqVO.setStatus(AfterSaleStatusEnum.APPLY.getStatus());
        reqVO.setWay(AfterSaleWayEnum.RETURN_AND_REFUND.getWay());
        reqVO.setType(AfterSaleTypeEnum.IN_SALE.getType());
        reqVO.setOrderNo("20221119084745002050001");
        reqVO.setSpuName("芋");
        reqVO.setCreateTime(new LocalDateTime[]{buildTime(2022, 1, 1), buildTime(2022, 1, 16)});

        // 调用
        PageResult<AfterSaleDO> pageResult = tradeAfterSaleService.getAfterSalePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbAfterSale, pageResult.getList().get(0));
    }
}
