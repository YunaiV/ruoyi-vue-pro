package cn.iocoder.yudao.module.infra.seatademo;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分布式事务示例账户 Mapper
 *
 * 在 Mapper 上添加 @DS 注解，保证 demo_seata_account 表的 SQL 都使用 seata_account 数据源
 *
 * @author 芋道源码
 */
@Mapper
@DS(DemoSeataAccountMapper.DATA_SOURCE)
public interface DemoSeataAccountMapper extends BaseMapperX<DemoSeataAccountDO> {

    /**
     * 账户库的数据源名，对应 spring.datasource.dynamic.datasource.seata_account 配置项
     */
    String DATA_SOURCE = "seata_account";

    default DemoSeataAccountDO selectByUserId(Long userId) {
        return selectOne(DemoSeataAccountDO::getUserId, userId);
    }

    /**
     * 扣减余额
     *
     * @param userId 用户编号
     * @param amount 扣减金额，必须大于 0
     * @return 更新条数；余额不足或账户不存在时为 0
     */
    default int updateBalanceDecr(Long userId, Integer amount) {
        LambdaUpdateWrapper<DemoSeataAccountDO> updateWrapper = new LambdaUpdateWrapper<DemoSeataAccountDO>()
                .setSql(" balance = balance - " + amount)
                .eq(DemoSeataAccountDO::getUserId, userId)
                .ge(DemoSeataAccountDO::getBalance, amount); // 条件扣减：余额足够才更新，避免扣成负数
        return update(null, updateWrapper);
    }

}
