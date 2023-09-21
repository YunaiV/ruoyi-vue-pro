package cn.iocoder.yudao.module.trade.service.withdraw;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.withdraw.vo.BrokerageWithdrawPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.withdraw.BrokerageWithdrawDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.withdraw.BrokerageWithdrawMapper;
import cn.iocoder.yudao.module.trade.service.brokerage.withdraw.BrokerageWithdrawServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO 芋艿：后续 review
/**
 * {@link BrokerageWithdrawServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(BrokerageWithdrawServiceImpl.class)
public class BrokerageWithdrawServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BrokerageWithdrawServiceImpl brokerageWithdrawService;

    @Resource
    private BrokerageWithdrawMapper brokerageWithdrawMapper;

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetBrokerageWithdrawPage() {
        // mock 数据
        BrokerageWithdrawDO dbBrokerageWithdraw = randomPojo(BrokerageWithdrawDO.class, o -> { // 等会查询到
            o.setUserId(null);
            o.setType(null);
            o.setName(null);
            o.setAccountNo(null);
            o.setBankName(null);
            o.setStatus(null);
            o.setCreateTime(null);
        });
        brokerageWithdrawMapper.insert(dbBrokerageWithdraw);
        // 测试 userId 不匹配
        brokerageWithdrawMapper.insert(cloneIgnoreId(dbBrokerageWithdraw, o -> o.setUserId(null)));
        // 测试 type 不匹配
        brokerageWithdrawMapper.insert(cloneIgnoreId(dbBrokerageWithdraw, o -> o.setType(null)));
        // 测试 name 不匹配
        brokerageWithdrawMapper.insert(cloneIgnoreId(dbBrokerageWithdraw, o -> o.setName(null)));
        // 测试 accountNo 不匹配
        brokerageWithdrawMapper.insert(cloneIgnoreId(dbBrokerageWithdraw, o -> o.setAccountNo(null)));
        // 测试 bankName 不匹配
        brokerageWithdrawMapper.insert(cloneIgnoreId(dbBrokerageWithdraw, o -> o.setBankName(null)));
        // 测试 status 不匹配
        brokerageWithdrawMapper.insert(cloneIgnoreId(dbBrokerageWithdraw, o -> o.setStatus(null)));
        // 测试 auditReason 不匹配
        brokerageWithdrawMapper.insert(cloneIgnoreId(dbBrokerageWithdraw, o -> o.setAuditReason(null)));
        // 测试 auditTime 不匹配
        brokerageWithdrawMapper.insert(cloneIgnoreId(dbBrokerageWithdraw, o -> o.setAuditTime(null)));
        // 测试 remark 不匹配
        brokerageWithdrawMapper.insert(cloneIgnoreId(dbBrokerageWithdraw, o -> o.setRemark(null)));
        // 测试 createTime 不匹配
        brokerageWithdrawMapper.insert(cloneIgnoreId(dbBrokerageWithdraw, o -> o.setCreateTime(null)));
        // 准备参数
        BrokerageWithdrawPageReqVO reqVO = new BrokerageWithdrawPageReqVO();
        reqVO.setUserId(null);
        reqVO.setType(null);
        reqVO.setName(null);
        reqVO.setAccountNo(null);
        reqVO.setBankName(null);
        reqVO.setStatus(null);
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<BrokerageWithdrawDO> pageResult = brokerageWithdrawService.getBrokerageWithdrawPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbBrokerageWithdraw, pageResult.getList().get(0));
    }

}
