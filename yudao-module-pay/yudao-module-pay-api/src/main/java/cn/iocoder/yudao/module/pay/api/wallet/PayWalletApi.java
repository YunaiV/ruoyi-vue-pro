package cn.iocoder.yudao.module.pay.api.wallet;

import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletCreateReqDto;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletRespDTO;

/**
 * 钱包 API 接口
 *
 * @author liurulin
 */
public interface PayWalletApi {

    // TODO @luchi：1）改成 addWalletBalance；2）PayWalletCreateReqDto 搞成 userId、userType；3）bizType 使用 integer，不然后续挪到 cloud 不好弄，因为枚举不好序列化
    /**
     * 添加钱包
     *
     * @param reqDTO 创建请求
     */
    void addWallet(PayWalletCreateReqDto reqDTO);

    // TODO @luchi：不用去 getWalletByUserId 钱包，直接添加余额就好。里面内部去创建。如果删除掉的化，PayWalletRespDTO 也删除哈。
    /**
     * 根据用户编号，获取钱包信息
     *
     * @param userId 用户id
     * @return 钱包信息
     */
    PayWalletRespDTO getWalletByUserId(Long userId);

}
