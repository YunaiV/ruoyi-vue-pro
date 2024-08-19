package cn.iocoder.yudao.module.pay.api.wallet;

import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletUpdateBalanceReqDTO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.MEMBER;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.WALLET_NOT_FOUND;

/**
 * 会员钱包 API 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
@Slf4j
public class PayWalletApiImpl implements PayWalletApi {

    @Resource
    private PayWalletService payWalletService;

    @Override
    public void updateBalance(PayWalletUpdateBalanceReqDTO reqDTO) {
        // 获得用户钱包
        PayWalletDO wallet = payWalletService.getOrCreateWallet(reqDTO.getUserId(), MEMBER.getValue());
        if (wallet == null) {
            log.error("[updateBalance]，reqDTO({}) 用户钱包不存在.", reqDTO);
            throw exception(WALLET_NOT_FOUND);
        }

        // 更新钱包余额
        payWalletService.addWalletBalance(wallet.getId(), String.valueOf(reqDTO.getUserId()),
                PayWalletBizTypeEnum.UPDATE_BALANCE, reqDTO.getBalance());
    }

}
