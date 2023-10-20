package cn.iocoder.yudao.module.pay.dal.mysql.wallet;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletRechargeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayWalletRechargeMapper extends BaseMapperX<PayWalletRechargeDO> {

    default int updateByIdAndPaid(Long id, boolean wherePayStatus, PayWalletRechargeDO updateObj) {
        return update(updateObj, new LambdaQueryWrapperX<PayWalletRechargeDO>()
                .eq(PayWalletRechargeDO::getId, id).eq(PayWalletRechargeDO::getPayStatus, wherePayStatus));
    }

    default int updateByIdAndRefunded(Long id, Integer whereRefundStatus, PayWalletRechargeDO updateObj) {
        return update(updateObj, new LambdaQueryWrapperX<PayWalletRechargeDO>()
                .eq(PayWalletRechargeDO::getId, id).eq(PayWalletRechargeDO::getRefundStatus, whereRefundStatus));
    }

}