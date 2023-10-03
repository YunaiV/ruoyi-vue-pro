package cn.iocoder.yudao.module.pay.dal.mysql.wallet;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import com.github.yulichang.toolkit.MPJWrappers;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Mapper
public interface PayWalletTransactionMapper extends BaseMapperX<PayWalletTransactionDO> {

    default PageResult<PayWalletTransactionDO> selectPage(Long walletId,
                                                          AppPayWalletTransactionPageReqVO pageReqVO) {
        LambdaQueryWrapperX<PayWalletTransactionDO> query = new LambdaQueryWrapperX<PayWalletTransactionDO>()
                .eq(PayWalletTransactionDO::getWalletId, walletId);
        if (Objects.equals(pageReqVO.getType(), AppPayWalletTransactionPageReqVO.TYPE_INCOME)) {
            query.gt(PayWalletTransactionDO::getPrice, 0);
        } else if (Objects.equals(pageReqVO.getType(), AppPayWalletTransactionPageReqVO.TYPE_EXPENSE)) {
            query.lt(PayWalletTransactionDO::getPrice, 0);
        }
        query.orderByDesc(PayWalletTransactionDO::getId);
        return selectPage(pageReqVO, query);
    }

    default PayWalletTransactionDO selectByNo(String no) {
        return selectOne(PayWalletTransactionDO::getNo, no);
    }

    default PayWalletTransactionDO selectByBiz(String bizId, Integer bizType) {
        return selectOne(PayWalletTransactionDO::getBizId, bizId,
                PayWalletTransactionDO::getBizType, bizType);
    }

    default Integer selectSummaryByBizTypeAndCreateTimeBetween(Integer type, LocalDateTime beginTime, LocalDateTime endTime) {
        return Optional.ofNullable(selectOne(MPJWrappers.<PayWalletTransactionDO>lambdaJoin()
                        .selectSum(PayWalletTransactionDO::getPrice)
                        .eq(PayWalletTransactionDO::getBizType, type)
                        .between(PayWalletTransactionDO::getCreateTime, beginTime, endTime)))
                .map(PayWalletTransactionDO::getPrice)
                .orElse(0);
    }

}




