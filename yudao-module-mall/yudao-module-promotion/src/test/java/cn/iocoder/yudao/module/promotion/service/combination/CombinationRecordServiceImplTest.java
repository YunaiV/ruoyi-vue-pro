package cn.iocoder.yudao.module.promotion.service.combination;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationProductDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.CombinationRecordMapper;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.trade.api.order.TradeOrderApi;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderCancelTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link CombinationRecordServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(CombinationRecordServiceImpl.class)
public class CombinationRecordServiceImplTest extends BaseDbUnitTest {

    private static final Long USER_ID = 100L;
    private static final Long ACTIVITY_ID = 200L;
    private static final Long SPU_ID = 300L;
    private static final Long SKU_ID = 400L;
    private static final Long HEAD_ID = 500L;
    private static final Long ORDER_ID = 600L;

    @Resource
    private CombinationRecordServiceImpl combinationRecordService;
    @Resource
    private CombinationRecordMapper combinationRecordMapper;

    @MockBean
    private CombinationActivityService combinationActivityService;
    @MockBean
    private MemberUserApi memberUserApi;
    @MockBean
    private ProductSpuApi productSpuApi;
    @MockBean
    private ProductSkuApi productSkuApi;
    @MockBean
    private TradeOrderApi tradeOrderApi;
    @MockBean
    private SocialClientApi socialClientApi;

    @Test
    public void testValidateCombinationRecord_headIdNullAsNewGroup() {
        // mock 数据
        CombinationActivityDO activity = mockValidateContext(10);

        // 调用
        KeyValue<CombinationActivityDO, CombinationProductDO> result = combinationRecordService
                .validateCombinationRecord(USER_ID, ACTIVITY_ID, null, SKU_ID, 1);

        // 断言
        assertSame(activity, result.getKey());
        assertEquals(SKU_ID, result.getValue().getSkuId());
    }

    @Test
    public void testValidateCombinationRecord_headIdGroupAsNewGroup() {
        // mock 数据：headId 为 0 时，也代表新开团
        CombinationActivityDO activity = mockValidateContext(10);

        // 调用
        KeyValue<CombinationActivityDO, CombinationProductDO> result = combinationRecordService
                .validateCombinationRecord(USER_ID, ACTIVITY_ID, CombinationRecordDO.HEAD_ID_GROUP, SKU_ID, 1);

        // 断言
        assertSame(activity, result.getKey());
        assertEquals(SKU_ID, result.getValue().getSkuId());
    }

    @Test
    public void testValidateCombinationRecord_realHeadIdAsJoinGroup() {
        // mock 数据：真实 headId 时，按参团校验父拼团
        CombinationActivityDO activity = mockValidateContext(10);
        combinationRecordMapper.insert(randomRecord(o -> {
            o.setId(HEAD_ID);
            o.setHeadId(CombinationRecordDO.HEAD_ID_GROUP);
            o.setUserId(101L);
            o.setOrderId(601L);
        }));

        // 调用
        KeyValue<CombinationActivityDO, CombinationProductDO> result = combinationRecordService
                .validateCombinationRecord(USER_ID, ACTIVITY_ID, HEAD_ID, SKU_ID, 1);

        // 断言
        assertSame(activity, result.getKey());
        assertEquals(SKU_ID, result.getValue().getSkuId());
    }

    @Test
    public void testValidateCombinationRecord_realHeadIdNotExists() {
        // mock 数据：拼团活动正常，但父拼团不存在
        mockActivity();

        // 调用，并断言
        assertServiceException(() -> combinationRecordService.validateCombinationRecord(
                USER_ID, ACTIVITY_ID, HEAD_ID, SKU_ID, 1), COMBINATION_RECORD_HEAD_NOT_EXISTS);
    }

    @Test
    public void testValidateCombinationRecord_countEqualsStock() {
        // mock 数据：库存刚好等于购买数量
        mockValidateContext(10);

        // 调用
        KeyValue<CombinationActivityDO, CombinationProductDO> result = combinationRecordService
                .validateCombinationRecord(USER_ID, ACTIVITY_ID, null, SKU_ID, 10);

        // 断言
        assertNotNull(result);
    }

    @Test
    public void testValidateCombinationRecord_countGreaterThanStock() {
        // mock 数据：库存不足
        mockValidateContext(10);

        // 调用，并断言
        assertServiceException(() -> combinationRecordService.validateCombinationRecord(
                USER_ID, ACTIVITY_ID, null, SKU_ID, 11), COMBINATION_ACTIVITY_UPDATE_STOCK_FAIL);
    }

    @Test
    public void testValidateCombinationRecord_haveJoined() {
        // mock 数据：用户存在进行中的拼团记录
        mockValidateContext(10);
        combinationRecordMapper.insert(randomRecord(o -> {
            o.setStatus(CombinationRecordStatusEnum.IN_PROGRESS.getStatus());
            o.setCount(1);
        }));

        // 调用，并断言
        assertServiceException(() -> combinationRecordService.validateCombinationRecord(
                USER_ID, ACTIVITY_ID, null, SKU_ID, 1), COMBINATION_RECORD_FAILED_HAVE_JOINED);
    }

    @Test
    public void testValidateCombinationRecord_totalLimitCountExceed() {
        // mock 数据：用户历史购买数量加本次数量超过总限购
        CombinationActivityDO activity = mockValidateContext(10);
        activity.setTotalLimitCount(5);
        combinationRecordMapper.insert(randomRecord(o -> {
            o.setStatus(CombinationRecordStatusEnum.SUCCESS.getStatus());
            o.setCount(3);
        }));

        // 调用，并断言
        assertServiceException(() -> combinationRecordService.validateCombinationRecord(
                USER_ID, ACTIVITY_ID, null, SKU_ID, 3), COMBINATION_RECORD_FAILED_TOTAL_LIMIT_COUNT_EXCEED);
    }

    @Test
    public void testCreateCombinationRecord_headIdGroupAsNewGroup() {
        // mock 数据：开团参数中 headId 为 0
        mockValidateContext(10);
        mockCreateContext();
        CombinationRecordCreateReqDTO reqDTO = randomCreateReqDTO(CombinationRecordDO.HEAD_ID_GROUP);

        // 调用
        CombinationRecordDO record = combinationRecordService.createCombinationRecord(reqDTO);

        // 断言：仍然按开团处理，不更新其它团记录
        assertNotNull(record.getId());
        assertEquals(CombinationRecordDO.HEAD_ID_GROUP, record.getHeadId());
        assertEquals(CombinationRecordStatusEnum.IN_PROGRESS.getStatus(), record.getStatus());
        assertEquals(1, record.getUserCount());
        assertFalse(record.getVirtualGroup());
        assertNotNull(record.getStartTime());
        assertNotNull(record.getExpireTime());
        assertEquals(1L, combinationRecordMapper.selectCount());
        assertEquals(CombinationRecordDO.HEAD_ID_GROUP,
                combinationRecordMapper.selectById(record.getId()).getHeadId());
    }

    @Test
    public void testHandleExpireRecord_cancelOrders() {
        // mock 数据：过期团长和一个团员
        CombinationRecordDO headRecord = randomRecord(o -> {
            o.setId(HEAD_ID);
            o.setHeadId(CombinationRecordDO.HEAD_ID_GROUP);
            o.setUserId(USER_ID);
            o.setOrderId(ORDER_ID);
        });
        CombinationRecordDO memberRecord = randomRecord(o -> {
            o.setId(501L);
            o.setHeadId(HEAD_ID);
            o.setUserId(101L);
            o.setOrderId(601L);
        });
        combinationRecordMapper.insert(headRecord);
        combinationRecordMapper.insert(memberRecord);

        // 调用
        combinationRecordService.handleExpireRecord(headRecord);

        // 断言：整团记录标记为失败，并取消已支付订单
        List<CombinationRecordDO> records = combinationRecordMapper.selectList();
        assertEquals(2, records.size());
        assertTrue(records.stream().allMatch(item ->
                CombinationRecordStatusEnum.FAILED.getStatus().equals(item.getStatus())));
        assertTrue(records.stream().allMatch(item -> item.getEndTime() != null));
        verify(tradeOrderApi).cancelPaidOrder(USER_ID, ORDER_ID, TradeOrderCancelTypeEnum.COMBINATION_CLOSE.getType());
        verify(tradeOrderApi).cancelPaidOrder(101L, 601L, TradeOrderCancelTypeEnum.COMBINATION_CLOSE.getType());
    }

    private CombinationActivityDO mockValidateContext(Integer stock) {
        CombinationActivityDO activity = mockActivity();
        CombinationProductDO product = randomPojo(CombinationProductDO.class, o -> {
            o.setActivityId(ACTIVITY_ID);
            o.setSpuId(SPU_ID);
            o.setSkuId(SKU_ID);
            o.setCombinationPrice(100);
        });
        ProductSkuRespDTO sku = randomPojo(ProductSkuRespDTO.class, o -> {
            o.setId(SKU_ID);
            o.setSpuId(SPU_ID);
            o.setStock(stock);
            o.setPicUrl("https://www.iocoder.cn/sku.png");
        });
        when(combinationActivityService.selectByActivityIdAndSkuId(ACTIVITY_ID, SKU_ID)).thenReturn(product);
        when(productSkuApi.getSku(SKU_ID)).thenReturn(sku);
        return activity;
    }

    private CombinationActivityDO mockActivity() {
        CombinationActivityDO activity = randomPojo(CombinationActivityDO.class, o -> {
            o.setId(ACTIVITY_ID);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setStartTime(LocalDateTime.now().minusHours(1));
            o.setEndTime(LocalDateTime.now().plusHours(1));
            o.setSingleLimitCount(100);
            o.setTotalLimitCount(100);
            o.setUserSize(3);
            o.setVirtualGroup(false);
            o.setLimitDuration(24);
        });
        when(combinationActivityService.validateCombinationActivityExists(ACTIVITY_ID)).thenReturn(activity);
        return activity;
    }

    private void mockCreateContext() {
        MemberUserRespDTO user = randomPojo(MemberUserRespDTO.class, o -> {
            o.setId(USER_ID);
            o.setNickname("芋道源码");
            o.setAvatar("https://www.iocoder.cn/avatar.png");
        });
        when(memberUserApi.getUser(USER_ID)).thenReturn(user);
        ProductSpuRespDTO spu = randomPojo(ProductSpuRespDTO.class, o -> {
            o.setId(SPU_ID);
            o.setName("测试商品");
            o.setPicUrl("https://www.iocoder.cn/spu.png");
        });
        when(productSpuApi.getSpu(SPU_ID)).thenReturn(spu);
    }

    private static CombinationRecordCreateReqDTO randomCreateReqDTO(Long headId) {
        return randomPojo(CombinationRecordCreateReqDTO.class, o -> {
            o.setActivityId(ACTIVITY_ID);
            o.setSpuId(SPU_ID);
            o.setSkuId(SKU_ID);
            o.setCount(1);
            o.setOrderId(ORDER_ID);
            o.setUserId(USER_ID);
            o.setHeadId(headId);
            o.setCombinationPrice(100);
        });
    }

    @SafeVarargs
    private static CombinationRecordDO randomRecord(Consumer<CombinationRecordDO>... consumers) {
        return randomPojo(CombinationRecordDO.class, o -> {
            o.setActivityId(ACTIVITY_ID);
            o.setCombinationPrice(100);
            o.setSpuId(SPU_ID);
            o.setSpuName("测试商品");
            o.setPicUrl("https://www.iocoder.cn/spu.png");
            o.setSkuId(SKU_ID);
            o.setCount(1);
            o.setUserId(USER_ID);
            o.setNickname("芋道源码");
            o.setAvatar("https://www.iocoder.cn/avatar.png");
            o.setHeadId(CombinationRecordDO.HEAD_ID_GROUP);
            o.setStatus(CombinationRecordStatusEnum.IN_PROGRESS.getStatus());
            o.setOrderId(ORDER_ID);
            o.setUserSize(3);
            o.setUserCount(1);
            o.setVirtualGroup(false);
            o.setStartTime(LocalDateTime.now().minusMinutes(10));
            o.setExpireTime(LocalDateTime.now().plusHours(1));
            o.setEndTime(null);
            for (Consumer<CombinationRecordDO> consumer : consumers) {
                consumer.accept(o);
            }
        });
    }

}
