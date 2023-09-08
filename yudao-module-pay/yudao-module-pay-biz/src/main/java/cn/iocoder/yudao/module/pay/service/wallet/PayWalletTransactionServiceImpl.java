package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletTransactionMapper;
import cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 钱包流水 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class PayWalletTransactionServiceImpl implements PayWalletTransactionService {

    @Resource
    private PayWalletService payWalletService;
    @Resource
    private PayWalletTransactionMapper payWalletTransactionMapper;

    @Override
    public PageResult<PayWalletTransactionDO> getWalletTransactionPage(Long userId, Integer userType,
                                                                       AppPayWalletTransactionPageReqVO pageVO) {
        PayWalletDO wallet = payWalletService.getOrCreateWallet(userId, userType);
        return payWalletTransactionMapper.selectPage(wallet.getId(), pageVO);
    }

    @Override
    public Long createWalletTransaction(PayWalletTransactionDO payWalletTransaction) {
         payWalletTransactionMapper.insert(payWalletTransaction);
         return payWalletTransaction.getId();
    }

    @Override
    public PayWalletTransactionDO getWalletTransactionByNo(String no) {
        return payWalletTransactionMapper.selectByNo(no);
    }

    @Override
    public PayWalletTransactionDO getWalletTransaction(String bizId, PayWalletBizTypeEnum type) {
        return payWalletTransactionMapper.selectByBiz(bizId, type.getType());
    }
}
