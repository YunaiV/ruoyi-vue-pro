package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.market.api.price.PriceApi;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.SpuInfoRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.orderitem.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderConfig;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomInteger;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author LeeYan9
 * @since 2022-09-07
 */
@Import({TradeOrderServiceImpl.class, TradeOrderConfig.class})
class TradeOrderServiceTest extends BaseDbUnitTest {


    @Resource
    TradeOrderService tradeOrderService;
    @Resource
    TradeOrderMapper tradeOrderMapper;
    @Resource
    TradeOrderItemMapper tradeOrderItemMapper;
    @MockBean
    ProductSpuApi productSpuApi;
    @MockBean
    ProductSkuApi productSkuApi;
    @MockBean
    PriceApi priceApi;
    @MockBean
    private PayOrderApi payOrderApi;

    @Test
    void testCreateTradeOrder_success() {
        // mock 商品SPU数据
        SpuInfoRespDTO spuInfoRespDTO = randomPojo(SpuInfoRespDTO.class, spuInfo -> {
            spuInfo.setId(1L);
            spuInfo.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(productSpuApi.getSpuList(Collections.singleton(1L))).thenReturn(Lists.newArrayList(spuInfoRespDTO));
        // mock 商品SkU数据
        ProductSkuRespDTO skuInfoRespDTO = randomPojo(ProductSkuRespDTO.class, skuInfo -> {
            skuInfo.setId(1L);
            skuInfo.setStatus(CommonStatusEnum.ENABLE.getStatus());
            skuInfo.setStock(randomInteger());
            skuInfo.setSpuId(1L);
        });
        when(productSkuApi.getSkuList(Collections.singleton(1L))).thenReturn(Lists.newArrayList(skuInfoRespDTO));
        // mock 价格信息
        PriceCalculateRespDTO calculateRespDTO = randomPojo(PriceCalculateRespDTO.class, priceCalculateRespDTO -> {
            PriceCalculateRespDTO.OrderItem item = priceCalculateRespDTO.getOrder().getItems().get(0);
            item.setSkuId(1L);
            item.setCount(2);
            priceCalculateRespDTO.getOrder().setItems(Collections.singletonList(item));
        });
        when(priceApi.calculatePrice(any())).thenReturn(calculateRespDTO);
        //mock 支付订单信息
        when(payOrderApi.createPayOrder(any())).thenReturn(1L);

        // 准备请求数据
        AppTradeOrderCreateReqVO tradeOrderCreateReqVO = randomPojo(AppTradeOrderCreateReqVO.class, reqVO -> {
            AppTradeOrderCreateReqVO.Item item = randomPojo(AppTradeOrderCreateReqVO.Item.class, o -> {
                o.setSkuId(1L);
                o.setCount(2);
            });
            reqVO.setItems(Collections.singletonList(item));
        });
        // 创建交易订单,支付订单记录
        Long payOrderId = tradeOrderService.createTradeOrder(1L, "127.0.0.1", tradeOrderCreateReqVO);
        //断言交易订单
        TradeOrderDO tradeOrderDO = tradeOrderMapper.selectOne(TradeOrderDO::getUserId, 1L);
        assertNotNull(tradeOrderDO);
        //价格&用户
        assertEquals(calculateRespDTO.getOrder().getPayPrice(), tradeOrderDO.getPayPrice());
        assertEquals(1L, tradeOrderDO.getUserId());
        //断言交易订单项
        TradeOrderItemDO tradeOrderItemDO = tradeOrderItemMapper.selectOne(TradeOrderItemDO::getOrderId, tradeOrderDO.getId());
        assertNotNull(tradeOrderDO);
        //商品&用户
        assertEquals(skuInfoRespDTO.getId(), tradeOrderItemDO.getSkuId());
        assertEquals(1L, tradeOrderItemDO.getUserId());
        //价格
        assertEquals(calculateRespDTO.getOrder().getItems().get(0).getPresentPrice(), tradeOrderItemDO.getPresentPrice());
    }

}
