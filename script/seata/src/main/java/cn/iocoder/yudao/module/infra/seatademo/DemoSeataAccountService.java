package cn.iocoder.yudao.module.infra.seatademo;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;

/**
 * 分布式事务示例账户 Service
 *
 * 作为全局事务中的一个分支：在 seata_account 数据源上执行本地事务，提交时由 Seata 注册分支、写入 undo_log
 *
 * @author 芋道源码
 */
@Service
@DS(DemoSeataAccountMapper.DATA_SOURCE) // 先切换到 seata_account 数据源，再开启 @Transactional 事务
@RequiredArgsConstructor // 使用构造方法注入，避免 JDK 8 与 JDK 17+ 分支的 @Resource 包名不同
public class DemoSeataAccountService {

    private final DemoSeataAccountMapper demoSeataAccountMapper;

    @Transactional(rollbackFor = Exception.class)
    public void deductBalance(Long userId, Integer amount) {
        Assert.notNull(userId, "用户编号不能为空");
        Assert.isTrue(amount != null && amount > 0, "扣减金额必须大于 0");
        int updateCount = demoSeataAccountMapper.updateBalanceDecr(userId, amount);
        if (updateCount == 0) {
            // 抛出异常，不要吞掉：本地事务回滚，并由全局事务的发起方回滚其它分支
            throw invalidParamException("用户({}) 的账户不存在或余额不足", userId);
        }
    }

    public DemoSeataAccountDO getAccount(Long userId) {
        return demoSeataAccountMapper.selectByUserId(userId);
    }

}
