package cn.iocoder.yudao.module.pay.dal.mysql.wallet;


import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayWalletMapper extends BaseMapperX<PayWalletDO> {

    default PayWalletDO selectByUserIdAndType(Long userId, Integer userType) {
        return selectOne(PayWalletDO::getUserId, userId, PayWalletDO::getUserType, userType);
    }
}




