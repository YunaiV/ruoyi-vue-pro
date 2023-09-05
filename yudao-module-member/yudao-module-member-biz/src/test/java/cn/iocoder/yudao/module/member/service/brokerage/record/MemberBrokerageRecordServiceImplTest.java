package cn.iocoder.yudao.module.member.service.brokerage.record;

import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.controller.admin.brokerage.record.vo.MemberBrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.brokerage.record.MemberBrokerageRecordDO;
import cn.iocoder.yudao.module.member.dal.mysql.brokerage.record.MemberBrokerageRecordMapper;
import cn.iocoder.yudao.module.member.service.point.MemberPointConfigService;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
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

/**
 * {@link MemberBrokerageRecordServiceImpl} 的单元测试类
 *
 * @author owen
 */
@Import(MemberBrokerageRecordServiceImpl.class)
public class MemberBrokerageRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MemberBrokerageRecordServiceImpl memberBrokerageRecordService;
    @Resource
    private MemberBrokerageRecordMapper memberBrokerageRecordMapper;

    @MockBean
    private MemberPointConfigService memberPointConfigService;
    @MockBean
    private MemberUserService memberUserService;

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetMemberBrokerageRecordPage() {
        // mock 数据
        MemberBrokerageRecordDO dbMemberBrokerageRecord = randomPojo(MemberBrokerageRecordDO.class, o -> { // 等会查询到
            o.setUserId(null);
            o.setBizType(null);
            o.setStatus(null);
            o.setCreateTime(null);
        });
        memberBrokerageRecordMapper.insert(dbMemberBrokerageRecord);
        // 测试 userId 不匹配
        memberBrokerageRecordMapper.insert(cloneIgnoreId(dbMemberBrokerageRecord, o -> o.setUserId(null)));
        // 测试 bizType 不匹配
        memberBrokerageRecordMapper.insert(cloneIgnoreId(dbMemberBrokerageRecord, o -> o.setBizType(null)));
        // 测试 status 不匹配
        memberBrokerageRecordMapper.insert(cloneIgnoreId(dbMemberBrokerageRecord, o -> o.setStatus(null)));
        // 测试 createTime 不匹配
        memberBrokerageRecordMapper.insert(cloneIgnoreId(dbMemberBrokerageRecord, o -> o.setCreateTime(null)));
        // 准备参数
        MemberBrokerageRecordPageReqVO reqVO = new MemberBrokerageRecordPageReqVO();
        reqVO.setUserId(null);
        reqVO.setBizType(null);
        reqVO.setStatus(null);
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<MemberBrokerageRecordDO> pageResult = memberBrokerageRecordService.getMemberBrokerageRecordPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbMemberBrokerageRecord, pageResult.getList().get(0));
    }

    @Test
    public void testCalculateBrokerage_useSkuBrokeragePrice() {
        // mock 数据
        Integer payPrice = randomInteger();
        Integer percent = randomInt(1, 101);
        Integer skuBrokeragePrice = randomInt();
        // 调用
        int brokerage = memberBrokerageRecordService.calculateBrokerage(payPrice, percent, skuBrokeragePrice);
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
        int brokerage = memberBrokerageRecordService.calculateBrokerage(payPrice, percent, skuBrokeragePrice);
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
        int brokerage = memberBrokerageRecordService.calculateBrokerage(payPrice, percent, skuBrokeragePrice);
        // 断言
        assertEquals(brokerage, 0);
    }
}
