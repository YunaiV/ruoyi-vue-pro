package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 钱包 Service 实现类
 *
 * @author jason
 */
@Service
public class PayWalletServiceImpl implements  PayWalletService {

    @Resource
    private PayWalletMapper payWalletMapper;

    @Override
    public PayWalletDO getPayWallet(Long userId, Integer userType) {
        return payWalletMapper.selectByUserIdAndType(userId, userType);
    }
}
