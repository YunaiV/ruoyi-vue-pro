package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.app.wallet.vo.AppPayWalletTransactionPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.enums.member.WalletBizTypeEnum;

/**
 * 钱包余额明细 Service 接口
 *
 * @author jason
 */
public interface PayWalletTransactionService {

    /**
     * 查询钱包余额明细, 分页
     *
     * @param userId   用户 id
     * @param userType 用户类型
     * @param pageVO   分页查询参数
     */
    PageResult<PayWalletTransactionDO> getWalletTransactionPage(Long userId, Integer userType,
                                                                AppPayWalletTransactionPageReqVO pageVO);

    /**
     * 新增钱包余额明细
     * @param payWalletTransaction 余额明细
     * @return id
     */
    Long addPayWalletTransaction(PayWalletTransactionDO payWalletTransaction);

    /**
     * 获取钱包余额明细 根据 no
     * @param no 流水号
     */
    PayWalletTransactionDO getPayWalletTransactionByNo(String no);

    /**
     * 获取钱包
     * @param walletId 钱包 id
     * @param bizId  业务编号
     * @param typeEnum  业务类型
     * @return 钱包余额明细
     */
    PayWalletTransactionDO getPayWalletTransaction(Long walletId, Long bizId, WalletBizTypeEnum typeEnum);
}
