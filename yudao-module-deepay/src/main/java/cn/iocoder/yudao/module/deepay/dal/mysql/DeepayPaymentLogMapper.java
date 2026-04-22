package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayPaymentLogDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeepayPaymentLogMapper extends BaseMapperX<DeepayPaymentLogDO> {

    /** 判断 paymentId 是否已成功处理（幂等查询）。 */
    default boolean existsPaid(String paymentId) {
        return selectCount(new LambdaQueryWrapper<DeepayPaymentLogDO>()
                .eq(DeepayPaymentLogDO::getPaymentId, paymentId)
                .eq(DeepayPaymentLogDO::getStatus, "PAID")) > 0;
    }

    /** 查询某 paymentId 的所有回调记录（排查重复回调）。 */
    default List<DeepayPaymentLogDO> selectByPaymentId(String paymentId) {
        return selectList(new LambdaQueryWrapper<DeepayPaymentLogDO>()
                .eq(DeepayPaymentLogDO::getPaymentId, paymentId)
                .orderByDesc(DeepayPaymentLogDO::getCreatedAt));
    }
}
