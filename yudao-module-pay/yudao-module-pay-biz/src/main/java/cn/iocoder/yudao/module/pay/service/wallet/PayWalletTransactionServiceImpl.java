package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.app.wallet.vo.AppPayWalletTransactionPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletTransactionMapper;
import cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.enums.member.WalletTransactionQueryTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.WALLET_NOT_FOUND;

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
        PayWalletDO wallet = payWalletService.getPayWallet(userId, userType);
        if (wallet == null) {
            log.error("[pageWalletTransaction] 用户 {} 钱包不存在", userId);
            throw exception(WALLET_NOT_FOUND);
        }
        // TODO @jason：不用 WalletTransactionQueryTypeEnum.valueOf(pageVO.getType()) 哈，直接 pageVO 里面判断值比对就好啦；
        return payWalletTransactionMapper.selectPageByWalletIdAndQueryType(wallet.getId(),
                WalletTransactionQueryTypeEnum.valueOf(pageVO.getType()), pageVO);
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
    public PayWalletTransactionDO getWalletTransaction(Long walletId, Long bizId, PayWalletBizTypeEnum type) {
        return payWalletTransactionMapper.selectByWalletIdAndBiz(walletId, bizId, type.getType());
    }

}
