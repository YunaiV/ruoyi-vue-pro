package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.app.wallet.vo.AppPayWalletTransactionPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletTransactionMapper;
import cn.iocoder.yudao.module.pay.enums.member.WalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.enums.member.WalletTransactionQueryTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.WALLET_NOT_FOUND;

/**
 * 钱包余额明细 Service 实现类
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
        PayWalletDO payWallet = payWalletService.getPayWallet(userId, userType);
        if (payWallet == null) {
            log.error("[pageWalletTransaction] 用户 {} 钱包不存在", userId);
            throw exception(WALLET_NOT_FOUND);
        }
        return payWalletTransactionMapper.selectPageByWalletIdAndQueryType(payWallet.getId(),
                WalletTransactionQueryTypeEnum.valueOf(pageVO.getType()), pageVO);
    }

    @Override
    public Long addPayWalletTransaction(PayWalletTransactionDO payWalletTransaction) {
         payWalletTransactionMapper.insert(payWalletTransaction);
         return payWalletTransaction.getId();
    }

    @Override
    public PayWalletTransactionDO getPayWalletTransactionByNo(String no) {
        return payWalletTransactionMapper.selectByNo(no);
    }

    @Override
    public PayWalletTransactionDO getPayWalletTransaction(Long walletId, Long bizId, WalletBizTypeEnum typeEnum) {
        return payWalletTransactionMapper.selectByWalletIdAndBiz(walletId, bizId, typeEnum.getBizType());
    }


}
