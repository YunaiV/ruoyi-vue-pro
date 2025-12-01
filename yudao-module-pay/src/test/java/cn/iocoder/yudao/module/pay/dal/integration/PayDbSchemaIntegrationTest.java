package cn.iocoder.yudao.module.pay.dal.integration;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbIntegrationTest;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.mysql.app.PayAppMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.channel.PayChannelMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.refund.PayRefundMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pay 模块数据库结构集成测试
 */
@DisplayName("Pay模块-数据库结构集成测试")
public class PayDbSchemaIntegrationTest extends BaseDbIntegrationTest {

    @Resource
    private PayAppMapper appMapper;

    @Resource
    private PayChannelMapper channelMapper;

    @Resource
    private PayOrderMapper orderMapper;

    @Resource
    private PayRefundMapper refundMapper;

    @Test
    @DisplayName("验证 pay_app 表结构")
    void testAppTable() {
        List<PayAppDO> list = appMapper.selectList();
        assertNotNull(list, "支付应用列表不应为空");
        System.out.println("pay_app 表记录数: " + list.size());
    }

    @Test
    @DisplayName("验证 pay_channel 表结构")
    void testChannelTable() {
        List<PayChannelDO> list = channelMapper.selectList();
        assertNotNull(list, "支付渠道列表不应为空");
        System.out.println("pay_channel 表记录数: " + list.size());
    }

    @Test
    @DisplayName("验证 pay_order 表结构")
    void testOrderTable() {
        List<PayOrderDO> list = orderMapper.selectList();
        assertNotNull(list, "支付订单列表不应为空");
        System.out.println("pay_order 表记录数: " + list.size());
    }

    @Test
    @DisplayName("验证 pay_refund 表结构")
    void testRefundTable() {
        List<PayRefundDO> list = refundMapper.selectList();
        assertNotNull(list, "退款订单列表不应为空");
        System.out.println("pay_refund 表记录数: " + list.size());
    }

    @Test
    @DisplayName("综合验证 - 所有核心表结构")
    void testAllCoreTables() {
        System.out.println("========== 开始综合验证 Pay 模块表结构 ==========");
        assertDoesNotThrow(() -> appMapper.selectList(), "pay_app 表查询失败");
        assertDoesNotThrow(() -> channelMapper.selectList(), "pay_channel 表查询失败");
        assertDoesNotThrow(() -> orderMapper.selectList(), "pay_order 表查询失败");
        assertDoesNotThrow(() -> refundMapper.selectList(), "pay_refund 表查询失败");
        System.out.println("========== Pay 模块表结构验证通过 ==========");
    }
}
