package cn.iocoder.yudao.module.trade.service.brokerage.record;

import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.record.vo.TradeBrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.record.TradeBrokerageRecordDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.record.TradeBrokerageRecordMapper;
import cn.iocoder.yudao.module.trade.service.brokerage.user.TradeBrokerageUserService;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.math.RoundingMode;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.hutool.core.util.RandomUtil.randomInt;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomInteger;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO @芋艿：单测后续看看
/**
 * {@link TradeBrokerageRecordServiceImpl} 的单元测试类
 *
 * @author owen
 */
@Import(TradeBrokerageRecordServiceImpl.class)
public class TradeBrokerageRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private TradeBrokerageRecordServiceImpl tradeBrokerageRecordService;
    @Resource
    private TradeBrokerageRecordMapper tradeBrokerageRecordMapper;

    @MockBean
    private TradeConfigService tradeConfigService;
    @MockBean
    private TradeBrokerageUserService tradeBrokerageUserService;

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetBrokerageRecordPage() {
        // mock 数据
        TradeBrokerageRecordDO dbBrokerageRecord = randomPojo(TradeBrokerageRecordDO.class, o -> { // 等会查询到
            o.setUserId(null);
            o.setBizType(null);
            o.setStatus(null);
            o.setCreateTime(null);
        });
        tradeBrokerageRecordMapper.insert(dbBrokerageRecord);
        // 测试 userId 不匹配
        tradeBrokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord, o -> o.setUserId(null)));
        // 测试 bizType 不匹配
        tradeBrokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord, o -> o.setBizType(null)));
        // 测试 status 不匹配
        tradeBrokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord, o -> o.setStatus(null)));
        // 测试 createTime 不匹配
        tradeBrokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord, o -> o.setCreateTime(null)));
        // 准备参数
        TradeBrokerageRecordPageReqVO reqVO = new TradeBrokerageRecordPageReqVO();
        reqVO.setUserId(null);
        reqVO.setBizType(null);
        reqVO.setStatus(null);
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<TradeBrokerageRecordDO> pageResult = tradeBrokerageRecordService.getBrokerageRecordPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbBrokerageRecord, pageResult.getList().get(0));
    }

    @Test
    public void testCalculateBrokerage_useSkuBrokeragePrice() {
        // mock 数据
        Integer payPrice = randomInteger();
        Integer percent = randomInt(1, 101);
        Integer skuBrokeragePrice = randomInt();
        // 调用
        int brokerage = tradeBrokerageRecordService.calculateBrokerage(payPrice, percent, skuBrokeragePrice);
        // 断言
        assertEquals(brokerage, skuBrokeragePrice);
    }

    @Test
    public void testCalculateBrokerage_usePercent() {
        // mock 数据
        Integer payPrice = randomInteger();
        Integer percent = randomInt(1, 101);
        Integer skuBrokeragePrice = randomEle(new Integer[]{0, null});
        System.out.println("skuBrokeragePrice=" + skuBrokeragePrice);
        // 调用
        int brokerage = tradeBrokerageRecordService.calculateBrokerage(payPrice, percent, skuBrokeragePrice);
        // 断言
        assertEquals(brokerage, NumberUtil.div(NumberUtil.mul(payPrice, percent), 100, 0, RoundingMode.DOWN).intValue());
    }

    @Test
    public void testCalculateBrokerage_equalsZero() {
        // mock 数据
        Integer payPrice = null;
        Integer percent = null;
        Integer skuBrokeragePrice = null;
        // 调用
        int brokerage = tradeBrokerageRecordService.calculateBrokerage(payPrice, percent, skuBrokeragePrice);
        // 断言
        assertEquals(brokerage, 0);
    }
}
