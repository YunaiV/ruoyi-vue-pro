package cn.iocoder.yudao.module.system.seatademo;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 分布式事务示例订单 Service
 *
 * 作为全局事务中的一个分支：在 seata_order 数据源上执行本地事务，提交时由 Seata 注册分支、写入 undo_log
 *
 * @author 芋道源码
 */
@Service
@DS(DemoSeataOrderMapper.DATA_SOURCE) // 先切换到 seata_order 数据源，再开启 @Transactional 事务
@RequiredArgsConstructor // 使用构造方法注入，避免 JDK 8 与 JDK 17+ 分支的 @Resource 包名不同
public class DemoSeataOrderService {

    private final DemoSeataOrderMapper demoSeataOrderMapper;

    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Long id, Long userId, Integer amount) {
        Assert.notNull(id, "订单编号不能为空");
        Assert.notNull(userId, "用户编号不能为空");
        Assert.isTrue(amount != null && amount > 0, "订单金额必须大于 0");
        DemoSeataOrderDO order = DemoSeataOrderDO.builder().id(id).userId(userId).amount(amount).build();
        demoSeataOrderMapper.insert(order); // 主键冲突时抛出 DuplicateKeyException，所在的全局事务回滚
    }

    public DemoSeataOrderDO getOrder(Long id) {
        return demoSeataOrderMapper.selectById(id);
    }

}
