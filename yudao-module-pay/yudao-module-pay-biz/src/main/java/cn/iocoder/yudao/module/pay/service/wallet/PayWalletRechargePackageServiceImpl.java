package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletRechargePackageDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletRechargePackageMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 钱包充值套餐 Service 实现类
 *
 * @author jason
 */
@Service
public class PayWalletRechargePackageServiceImpl implements PayWalletRechargePackageService {

    @Resource
    private PayWalletRechargePackageMapper rechargePackageMapper;

    @Override
    public PayWalletRechargePackageDO getWalletRechargePackage(Long packageId) {
        return rechargePackageMapper.selectById(packageId);
    }
}
