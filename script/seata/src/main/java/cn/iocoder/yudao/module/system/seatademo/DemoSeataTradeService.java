package cn.iocoder.yudao.module.system.seatademo;

import cn.iocoder.yudao.module.infra.seatademo.DemoSeataAccountService;
import lombok.RequiredArgsConstructor;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

/**
 * 分布式事务示例下单 Service，即全局事务的入口（发起方）
 *
 * 单体项目中，订单、账户是同一个进程的两个数据源（seata_order、seata_account），直接调用账户 Service
 *
 * @author 芋道源码
 */
@Service // 使用 @Service 组件扫描注册：Seata 2.6 不会增强 @Bean 方法定义的 Bean，@GlobalTransactional 会失效
@RequiredArgsConstructor // 使用构造方法注入，避免 JDK 8 与 JDK 17+ 分支的 @Resource 包名不同
public class DemoSeataTradeService {

    private final DemoSeataOrderService demoSeataOrderService;
    private final DemoSeataAccountService demoSeataAccountService;

    /**
     * 下单：创建订单 + 扣减余额，两个数据源的操作要么都成功，要么都回滚
     *
     * 注意，这里只添加 @GlobalTransactional，【不要】添加 @Transactional：
     * 本地事务会在开始时固定一个数据源的连接，导致后续的 @DS 切换失效，两个分支的事务由各自的 Service 开启
     *
     * @param orderId 订单编号，由调用方生成，作为幂等主键
     * @param userId  用户编号
     * @param amount  订单金额，单位：分
     */
    @GlobalTransactional(name = "demo-seata-place-order", rollbackFor = Exception.class)
    public void placeOrder(Long orderId, Long userId, Integer amount) {
        // 1. 创建订单：分支一，本地事务提交后，订单已经写入 seata_order 库，同时记录 undo_log
        demoSeataOrderService.createOrder(orderId, userId, amount);
        // 2. 扣减余额：分支二；余额不足时抛出异常，Seata 根据 undo_log 删除分支一已经提交的订单
        demoSeataAccountService.deductBalance(userId, amount);
    }

}
