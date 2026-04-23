package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface DeepayOrderMapper extends BaseMapperX<DeepayOrderDO> {

    default DeepayOrderDO selectByPaymentId(String paymentId) {
        return selectOne(new LambdaQueryWrapper<DeepayOrderDO>()
                .eq(DeepayOrderDO::getPaymentId, paymentId));
    }

    /**
     * 根据链码和用户 ID 查询订单（唯一性校验）。
     */
    default DeepayOrderDO selectByChainCodeAndUserId(String chainCode, Long userId) {
        return selectOne(new LambdaQueryWrapper<DeepayOrderDO>()
                .eq(DeepayOrderDO::getChainCode, chainCode)
                .eq(DeepayOrderDO::getUserId, userId));
    }

    /**
     * 原子性标记订单为 PAID。
     * 使用 WHERE status = 'PENDING' 保证幂等：已 PAID 的订单不会被重复更新。
     *
     * @return 受影响行数；0 表示订单已处理（幂等场景）
     */
    default int markPaid(String paymentId) {
        return update(null, new LambdaUpdateWrapper<DeepayOrderDO>()
                .eq(DeepayOrderDO::getPaymentId, paymentId)
                .eq(DeepayOrderDO::getStatus, "PENDING")
                .set(DeepayOrderDO::getStatus, "PAID")
                .set(DeepayOrderDO::getPaidAt, LocalDateTime.now()));
    }

    /**
     * 原子性标记订单为 CANCELLED（退款场景）。
     * 仅允许从 PENDING 或 PAID 状态转入。
     *
     * @return 受影响行数；0 表示已取消或状态不合法
     */
    default int markCancelled(String paymentId) {
        return update(null, new LambdaUpdateWrapper<DeepayOrderDO>()
                .eq(DeepayOrderDO::getPaymentId, paymentId)
                .in(DeepayOrderDO::getStatus, "PENDING", "PAID")
                .set(DeepayOrderDO::getStatus, "CANCELLED"));
    }

}
