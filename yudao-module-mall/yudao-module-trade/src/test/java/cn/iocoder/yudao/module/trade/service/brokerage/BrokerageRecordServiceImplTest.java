package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.record.AppBrokerageProductPriceRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.BrokerageRecordMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.RoundingMode;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * {@link BrokerageRecordServiceImpl} 的单元测试类
 *
 * @author owen
 */
@Import(BrokerageRecordServiceImpl.class)
public class BrokerageRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BrokerageRecordServiceImpl brokerageRecordService;
    @Resource
    private BrokerageRecordMapper brokerageRecordMapper;

    @MockitoBean
    private TradeConfigService tradeConfigService;
    @MockitoBean
    private BrokerageUserService brokerageUserService;
    @MockitoBean
    private ProductSpuApi productSpuApi;
    @MockitoBean
    private ProductSkuApi productSkuApi;

    @Test
    public void testGetBrokerageRecordPage() {
        // mock 数据
        BrokerageRecordDO dbBrokerageRecord = randomPojo(BrokerageRecordDO.class, o -> { // 等会查询到
            o.setUserId(1L);
            o.setBizType(BrokerageRecordBizTypeEnum.ORDER.getType());
            o.setStatus(BrokerageRecordStatusEnum.SETTLEMENT.getStatus());
            o.setSourceUserLevel(1);
            o.setSourceUserId(100L);
            o.setCreateTime(buildTime(2023, 2, 10));
            o.setDeleted(false);
        });
        brokerageRecordMapper.insert(dbBrokerageRecord);
        // 测试 userId 不匹配
        brokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord, o -> o.setUserId(2L)));
        // 测试 bizType 不匹配
        brokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord,
                o -> o.setBizType(BrokerageRecordBizTypeEnum.WITHDRAW.getType())));
        // 测试 status 不匹配
        brokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord,
                o -> o.setStatus(BrokerageRecordStatusEnum.CANCEL.getStatus())));
        // 测试 sourceUserLevel 不匹配
        brokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord, o -> o.setSourceUserLevel(2)));
        // 测试 createTime 不匹配
        brokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord,
                o -> o.setCreateTime(buildTime(2023, 3, 1))));
        // 准备参数
        BrokerageRecordPageReqVO reqVO = new BrokerageRecordPageReqVO();
        reqVO.setUserId(1L);
        reqVO.setBizType(BrokerageRecordBizTypeEnum.ORDER.getType());
        reqVO.setStatus(BrokerageRecordStatusEnum.SETTLEMENT.getStatus());
        reqVO.setSourceUserLevel(1);
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<BrokerageRecordDO> pageResult = brokerageRecordService.getBrokerageRecordPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbBrokerageRecord, pageResult.getList().get(0));
    }

    @Test
    public void testCalculatePrice_useFixedPrice() {
        // mock 数据
        Integer payPrice = 1000;
        Integer percent = 10;
        Integer fixedPrice = 88;
        // 调用
        int brokerage = brokerageRecordService.calculatePrice(payPrice, percent, fixedPrice);
        // 断言
        assertEquals(fixedPrice, brokerage);
    }

    @Test
    public void testCalculatePrice_usePercent() {
        // mock 数据：fixedPrice 为 null 时，按比例计算佣金
        Integer payPrice = 1000;
        Integer percent = 10;
        Integer fixedPrice = null;
        // 调用
        int brokerage = brokerageRecordService.calculatePrice(payPrice, percent, fixedPrice);
        // 断言
        assertEquals(NumberUtil.div(NumberUtil.mul(payPrice, percent), 100, 0,
                RoundingMode.DOWN).intValue(), brokerage);
    }

    @Test
    public void testCalculatePrice_fixedPriceIsZero() {
        // mock 数据：fixedPrice 为 0 时，仍使用固定佣金，不回退到比例计算
        Integer payPrice = 1000;
        Integer percent = 10;
        Integer fixedPrice = 0;
        // 调用
        int brokerage = brokerageRecordService.calculatePrice(payPrice, percent, fixedPrice);
        // 断言
        assertEquals(0, brokerage);
    }

    @Test
    public void testCalculatePrice_equalsZero() {
        // mock 数据：三个参数均无效时，返回 0
        Integer payPrice = null;
        Integer percent = null;
        Integer fixedPrice = null;
        // 调用
        int brokerage = brokerageRecordService.calculatePrice(payPrice, percent, fixedPrice);
        // 断言
        assertEquals(0, brokerage);
    }

    @Test
    public void testCalculateProductBrokeragePrice_globalPercent() {
        // mock：分销功能已开启，比例 10%，当前用户有分销资格
        TradeConfigDO tradeConfig = new TradeConfigDO();
        tradeConfig.setBrokerageEnabled(true);
        tradeConfig.setBrokerageFirstPercent(10);
        when(tradeConfigService.getTradeConfig()).thenReturn(tradeConfig);
        when(brokerageUserService.getUserBrokerageEnabled(100L)).thenReturn(true);

        // mock：商品未开启独立分销（subCommissionType = false），SKU 售价 1000 分
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setSubCommissionType(false);
        when(productSpuApi.getSpu(1L)).thenReturn(spu);

        ProductSkuRespDTO sku = new ProductSkuRespDTO();
        sku.setPrice(1000);
        when(productSkuApi.getSkuListBySpuId(ListUtil.of(1L))).thenReturn(ListUtil.of(sku));

        // 调用
        AppBrokerageProductPriceRespVO result = brokerageRecordService.calculateProductBrokeragePrice(100L, 1L);

        // 断言：按 10% 比例计算，1000 * 10% = 100 分（向下取整）
        assertTrue(result.getEnabled());
        assertEquals(100, result.getBrokerageMinPrice());
        assertEquals(100, result.getBrokerageMaxPrice());
    }

    @Test
    public void testCalculateProductBrokeragePrice_subCommissionZeroFixed() {
        // mock：分销功能已开启，比例 10%，当前用户有分销资格
        TradeConfigDO tradeConfig = new TradeConfigDO();
        tradeConfig.setBrokerageEnabled(true);
        tradeConfig.setBrokerageFirstPercent(10);
        when(tradeConfigService.getTradeConfig()).thenReturn(tradeConfig);
        when(brokerageUserService.getUserBrokerageEnabled(100L)).thenReturn(true);

        // mock：商品开启独立分销（subCommissionType = true），SKU 固定佣金为 0（商家主动设置）
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setSubCommissionType(true);
        when(productSpuApi.getSpu(2L)).thenReturn(spu);

        ProductSkuRespDTO sku = new ProductSkuRespDTO();
        sku.setPrice(1000);
        sku.setFirstBrokeragePrice(0);
        when(productSkuApi.getSkuListBySpuId(ListUtil.of(2L))).thenReturn(ListUtil.of(sku));

        // 调用
        AppBrokerageProductPriceRespVO result = brokerageRecordService.calculateProductBrokeragePrice(100L, 2L);

        // 断言：独立分销固定佣金为 0，应返回 0，不得回退到全局比例
        assertTrue(result.getEnabled());
        assertEquals(0, result.getBrokerageMinPrice());
        assertEquals(0, result.getBrokerageMaxPrice());
    }

    @Test
    public void testCalculateProductBrokeragePrice_subCommissionNullFixed() {
        // mock：分销功能已开启，比例 10%，当前用户有分销资格
        TradeConfigDO tradeConfig = new TradeConfigDO();
        tradeConfig.setBrokerageEnabled(true);
        tradeConfig.setBrokerageFirstPercent(10);
        when(tradeConfigService.getTradeConfig()).thenReturn(tradeConfig);
        when(brokerageUserService.getUserBrokerageEnabled(100L)).thenReturn(true);

        // mock：商品开启独立分销，其中一个 SKU 固定佣金未配置
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setSubCommissionType(true);
        when(productSpuApi.getSpu(3L)).thenReturn(spu);

        ProductSkuRespDTO nullBrokerageSku = new ProductSkuRespDTO();
        nullBrokerageSku.setPrice(1000);
        nullBrokerageSku.setFirstBrokeragePrice(null);
        ProductSkuRespDTO fixedBrokerageSku = new ProductSkuRespDTO();
        fixedBrokerageSku.setPrice(2000);
        fixedBrokerageSku.setFirstBrokeragePrice(200);
        when(productSkuApi.getSkuListBySpuId(ListUtil.of(3L))).thenReturn(ListUtil.of(nullBrokerageSku, fixedBrokerageSku));

        // 调用
        AppBrokerageProductPriceRespVO result = brokerageRecordService.calculateProductBrokeragePrice(100L, 3L);

        // 断言：独立分销固定佣金为空时按 0 处理，避免比较最小/最大佣金时报错
        assertTrue(result.getEnabled());
        assertEquals(0, result.getBrokerageMinPrice());
        assertEquals(200, result.getBrokerageMaxPrice());
    }

    @Test
    public void testCalculateProductBrokeragePrice_subCommissionEmptySkuList() {
        // mock：分销功能已开启，比例 10%，当前用户有分销资格
        TradeConfigDO tradeConfig = new TradeConfigDO();
        tradeConfig.setBrokerageEnabled(true);
        tradeConfig.setBrokerageFirstPercent(10);
        when(tradeConfigService.getTradeConfig()).thenReturn(tradeConfig);
        when(brokerageUserService.getUserBrokerageEnabled(100L)).thenReturn(true);

        // mock：商品开启独立分销，但查询不到 SKU 固定佣金
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setSubCommissionType(true);
        when(productSpuApi.getSpu(4L)).thenReturn(spu);
        when(productSkuApi.getSkuListBySpuId(ListUtil.of(4L))).thenReturn(ListUtil.of());

        // 调用
        AppBrokerageProductPriceRespVO result = brokerageRecordService.calculateProductBrokeragePrice(100L, 4L);

        // 断言：独立分销没有固定佣金时，不回退到全局比例
        assertTrue(result.getEnabled());
        assertEquals(0, result.getBrokerageMinPrice());
        assertEquals(0, result.getBrokerageMaxPrice());
    }
}
