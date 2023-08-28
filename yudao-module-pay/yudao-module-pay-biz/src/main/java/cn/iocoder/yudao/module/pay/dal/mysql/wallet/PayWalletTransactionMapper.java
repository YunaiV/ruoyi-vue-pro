package cn.iocoder.yudao.module.pay.dal.mysql.wallet;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.enums.member.WalletTransactionQueryTypeEnum;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayWalletTransactionMapper extends BaseMapperX<PayWalletTransactionDO> {

    default PageResult<PayWalletTransactionDO> selectPageByWalletIdAndQueryType(Long walletId,
                                                                                WalletTransactionQueryTypeEnum queryType,
                                                                                PageParam pageParam) {
        LambdaQueryWrapperX<PayWalletTransactionDO> query = new LambdaQueryWrapperX<PayWalletTransactionDO>()
                .eq(PayWalletTransactionDO::getWalletId, walletId);
        if (WalletTransactionQueryTypeEnum.RECHARGE == queryType ) {
            query.ge(PayWalletTransactionDO::getAmount, 0);
        }
        if (WalletTransactionQueryTypeEnum.EXPENSE == queryType ) {
            query.lt(PayWalletTransactionDO::getAmount, 0);
        }
        query.orderByDesc(PayWalletTransactionDO::getTransactionTime);
        return selectPage(pageParam, query);
    }

    default PayWalletTransactionDO selectByNo(String no) {
        return selectOne(PayWalletTransactionDO::getNo, no);
    }

    default PayWalletTransactionDO selectByWalletIdAndBiz(Long walletId, Long bizId, Integer bizType) {
        return selectOne(PayWalletTransactionDO::getWalletId, walletId, PayWalletTransactionDO::getBizId,
                bizId, PayWalletTransactionDO::getBizType, bizType);
    }
}




