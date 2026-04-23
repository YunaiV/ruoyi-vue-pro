package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface DeepayOrderMapper extends BaseMapperX<DeepayOrderDO> {

    default DeepayOrderDO selectByChainCode(String chainCode) {
        return selectOne(new LambdaQueryWrapper<DeepayOrderDO>()
                .eq(DeepayOrderDO::getChainCode, chainCode)
                .orderByDesc(DeepayOrderDO::getId)
                .last("LIMIT 1"));
    }

    default DeepayOrderDO selectByChainCodeAndUserId(String chainCode, Long userId) {
        return selectOne(new LambdaQueryWrapper<DeepayOrderDO>()
                .eq(DeepayOrderDO::getChainCode, chainCode)
                .eq(DeepayOrderDO::getUserId, userId)
                .orderByDesc(DeepayOrderDO::getId)
                .last("LIMIT 1"));
    }

    default DeepayOrderDO selectByPaymentId(String paymentId) {
        return selectOne(new LambdaQueryWrapper<DeepayOrderDO>()
                .eq(DeepayOrderDO::getPaymentId, paymentId));
    }

    /**
     * 原子标记支付成功（WHERE status='PENDING' 保证幂等）。
     *
     * @return 受影响行数；0 表示订单已被处理（并发幂等）
     */
    default int markPaid(String paymentId) {
        return update(null, new LambdaUpdateWrapper<DeepayOrderDO>()
                .eq(DeepayOrderDO::getPaymentId, paymentId)
                .eq(DeepayOrderDO::getStatus, "PENDING")
                .set(DeepayOrderDO::getStatus, "PAID")
                .set(DeepayOrderDO::getUpdatedAt, LocalDateTime.now()));
    }

    default void markCancelled(String paymentId) {
        update(null, new LambdaUpdateWrapper<DeepayOrderDO>()
                .eq(DeepayOrderDO::getPaymentId, paymentId)
                .set(DeepayOrderDO::getStatus, "CANCELLED")
                .set(DeepayOrderDO::getUpdatedAt, LocalDateTime.now()));
    }

    default void updateStatusByChainCode(String chainCode, String status) {
        update(null, new LambdaUpdateWrapper<DeepayOrderDO>()
                .eq(DeepayOrderDO::getChainCode, chainCode)
                .set(DeepayOrderDO::getStatus, status)
                .set(DeepayOrderDO::getUpdatedAt, LocalDateTime.now()));
    }

}
