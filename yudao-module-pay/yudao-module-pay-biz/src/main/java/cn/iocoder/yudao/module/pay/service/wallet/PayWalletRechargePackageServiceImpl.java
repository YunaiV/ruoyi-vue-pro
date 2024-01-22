package cn.iocoder.yudao.module.pay.service.wallet;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackageCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackagePageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackageUpdateReqVO;
import cn.iocoder.yudao.module.pay.convert.wallet.PayWalletRechargePackageConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletRechargePackageDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletRechargePackageMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 钱包充值套餐 Service 实现类
 *
 * @author jason
 */
@Service
public class PayWalletRechargePackageServiceImpl implements PayWalletRechargePackageService {

    @Resource
    private PayWalletRechargePackageMapper walletRechargePackageMapper;

    @Override
    public PayWalletRechargePackageDO getWalletRechargePackage(Long packageId) {
        return walletRechargePackageMapper.selectById(packageId);
    }

    @Override
    public PayWalletRechargePackageDO validWalletRechargePackage(Long packageId) {
        PayWalletRechargePackageDO rechargePackageDO = walletRechargePackageMapper.selectById(packageId);
        if (rechargePackageDO == null) {
            throw exception(WALLET_RECHARGE_PACKAGE_NOT_FOUND);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(rechargePackageDO.getStatus())) {
            throw exception(WALLET_RECHARGE_PACKAGE_IS_DISABLE);
        }
        return rechargePackageDO;
    }

    @Override
    public Long createWalletRechargePackage(WalletRechargePackageCreateReqVO createReqVO) {
        // 校验套餐名是否唯一
        validateRechargePackageNameUnique(null, createReqVO.getName());

        // 插入
        PayWalletRechargePackageDO walletRechargePackage = PayWalletRechargePackageConvert.INSTANCE.convert(createReqVO);
        walletRechargePackageMapper.insert(walletRechargePackage);
        // 返回
        return walletRechargePackage.getId();
    }

    @Override
    public void updateWalletRechargePackage(WalletRechargePackageUpdateReqVO updateReqVO) {
        // 校验存在
        validateWalletRechargePackageExists(updateReqVO.getId());
        // 校验套餐名是否唯一
        validateRechargePackageNameUnique(updateReqVO.getId(), updateReqVO.getName());

        // 更新
        PayWalletRechargePackageDO updateObj = PayWalletRechargePackageConvert.INSTANCE.convert(updateReqVO);
        walletRechargePackageMapper.updateById(updateObj);
    }

    private void validateRechargePackageNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        PayWalletRechargePackageDO rechargePackage = walletRechargePackageMapper.selectByName(name);
        if (rechargePackage == null) {
            return ;
        }
        if (id == null) {
            throw exception(WALLET_RECHARGE_PACKAGE_NAME_EXISTS);
        }
        if (!id.equals(rechargePackage.getId())) {
            throw exception(WALLET_RECHARGE_PACKAGE_NAME_EXISTS);
        }
    }

    @Override
    public void deleteWalletRechargePackage(Long id) {
        // 校验存在
        validateWalletRechargePackageExists(id);
        // 删除
        walletRechargePackageMapper.deleteById(id);
    }

    private void validateWalletRechargePackageExists(Long id) {
        if (walletRechargePackageMapper.selectById(id) == null) {
            throw exception(WALLET_RECHARGE_PACKAGE_NOT_FOUND);
        }
    }

    @Override
    public PageResult<PayWalletRechargePackageDO> getWalletRechargePackagePage(WalletRechargePackagePageReqVO pageReqVO) {
        return walletRechargePackageMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayWalletRechargePackageDO> getWalletRechargePackageList(Integer status) {
        return walletRechargePackageMapper.selectListByStatus(status);
    }

}
