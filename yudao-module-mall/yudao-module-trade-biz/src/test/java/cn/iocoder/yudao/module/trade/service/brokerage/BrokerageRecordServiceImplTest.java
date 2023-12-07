package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.BrokerageRecordMapper;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
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
 * {@link BrokerageRecordServiceImpl} 的单元测试类
 *
 * @author owen
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(BrokerageRecordServiceImpl.class)
public class BrokerageRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BrokerageRecordServiceImpl brokerageRecordService;
    @Resource
    private BrokerageRecordMapper brokerageRecordMapper;

    @MockBean
    private TradeConfigService tradeConfigService;
    @MockBean
    private BrokerageUserService brokerageUserService;

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetBrokerageRecordPage() {
        // mock 数据
        BrokerageRecordDO dbBrokerageRecord = randomPojo(BrokerageRecordDO.class, o -> { // 等会查询到
            o.setUserId(null);
            o.setBizType(null);
            o.setStatus(null);
            o.setCreateTime(null);
        });
        brokerageRecordMapper.insert(dbBrokerageRecord);
        // 测试 userId 不匹配
        brokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord, o -> o.setUserId(null)));
        // 测试 bizType 不匹配
        brokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord, o -> o.setBizType(null)));
        // 测试 status 不匹配
        brokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord, o -> o.setStatus(null)));
        // 测试 createTime 不匹配
        brokerageRecordMapper.insert(cloneIgnoreId(dbBrokerageRecord, o -> o.setCreateTime(null)));
        // 准备参数
        BrokerageRecordPageReqVO reqVO = new BrokerageRecordPageReqVO();
        reqVO.setUserId(null);
        reqVO.setBizType(null);
        reqVO.setStatus(null);
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
        Integer payPrice = randomInteger();
        Integer percent = randomInt(1, 101);
        Integer fixedPrice = randomInt();
        // 调用
        int brokerage = brokerageRecordService.calculatePrice(payPrice, percent, fixedPrice);
        // 断言
        assertEquals(brokerage, fixedPrice);
    }

    @Test
    public void testCalculatePrice_usePercent() {
        // mock 数据
        Integer payPrice = randomInteger();
        Integer percent = randomInt(1, 101);
        Integer fixedPrice = randomEle(new Integer[]{0, null});
        System.out.println("fixedPrice=" + fixedPrice);
        // 调用
        int brokerage = brokerageRecordService.calculatePrice(payPrice, percent, fixedPrice);
        // 断言
        assertEquals(brokerage, NumberUtil.div(NumberUtil.mul(payPrice, percent), 100, 0, RoundingMode.DOWN).intValue());
    }

    @Test
    public void testCalculatePrice_equalsZero() {
        // mock 数据
        Integer payPrice = null;
        Integer percent = null;
        Integer fixedPrice = null;
        // 调用
        int brokerage = brokerageRecordService.calculatePrice(payPrice, percent, fixedPrice);
        // 断言
        assertEquals(brokerage, 0);
    }
}
