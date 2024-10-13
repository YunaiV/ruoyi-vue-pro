package cn.iocoder.yudao.module.pay.api.wallet;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletCreateReqDto;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletRespDTO;
import cn.iocoder.yudao.module.pay.convert.wallet.PayWalletConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 钱包 API 实现类
 *
 * @author 芋道源码
 */
@Service
public class PayWalletApiImpl implements PayWalletApi {

    @Resource
    private PayWalletService payWalletService;

    @Override
    public void addWallet(PayWalletCreateReqDto reqDTO) {
        //添加钱包金额
        payWalletService.addWalletBalance(reqDTO.getWalletId(), reqDTO.getBizId(), reqDTO.getBizType(), reqDTO.getPrice());
    }

    @Override
    public PayWalletRespDTO getWalletByUserId(Long userId) {
        PayWalletDO orCreateWallet = payWalletService.getOrCreateWallet(userId, UserTypeEnum.MEMBER.getValue());
        return PayWalletConvert.INSTANCE.convert03(orCreateWallet);
    }
}
