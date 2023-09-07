package cn.iocoder.yudao.module.pay.dal.mysql.wallet;


import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayWalletMapper extends BaseMapperX<PayWalletDO> {

    default PayWalletDO selectByUserIdAndType(Long userId, Integer userType) {
        return selectOne(PayWalletDO::getUserId, userId,
                PayWalletDO::getUserType, userType);
    }

    /**
     * 当余额减少时候更新
     *
     * @param bizType 业务类型
     * @param balance 当前余额
     * @param totalRecharge 当前累计充值
     * @param totalExpense 当前累计支出
     * @param price 支出的金额
     * @param id 钱包 id
     */
    default int updateWhenDecBalance(PayWalletBizTypeEnum bizType, Integer balance, Long totalRecharge,
                                     Long totalExpense, Integer price, Long id) {
        PayWalletDO updateDO = new PayWalletDO().setBalance(balance - price);
        if(bizType == PayWalletBizTypeEnum.PAYMENT){
            updateDO.setTotalExpense(totalExpense + price);
        }
        if (bizType == PayWalletBizTypeEnum.RECHARGE_REFUND) {
            updateDO.setTotalRecharge(totalRecharge - price);
        }
        return update(updateDO,
                new LambdaQueryWrapper<PayWalletDO>().eq(PayWalletDO::getId, id)
                        .eq(PayWalletDO::getBalance, balance)
                        .ge(PayWalletDO::getBalance, price));
    }

    /**
     * 当余额增加时候更新
     *
     * @param bizType  业务类型
     * @param balance  当前余额
     * @param totalRecharge 当前累计充值
     * @param totalExpense 当前累计支出
     * @param price 金额
     * @param id 钱包 id
     */
    default int updateWhenIncBalance(PayWalletBizTypeEnum bizType, Integer balance, Long totalRecharge,
                                     Long totalExpense, Integer price, Long id) {
        PayWalletDO updateDO = new PayWalletDO().setBalance(balance + price);
        if (bizType == PayWalletBizTypeEnum.PAYMENT_REFUND) {
            updateDO.setTotalExpense(totalExpense - price);
        }
        if (bizType == PayWalletBizTypeEnum.RECHARGE) {
            updateDO.setTotalExpense(totalRecharge + price);
        }
        return update(updateDO,
                new LambdaQueryWrapper<PayWalletDO>().eq(PayWalletDO::getId, id)
                        .eq(PayWalletDO::getBalance, balance));
    }
}




