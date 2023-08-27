package cn.iocoder.yudao.module.pay.convert.wallet;

import cn.iocoder.yudao.module.pay.controller.app.wallet.vo.AppPayWalletRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayWalletConvert {

    PayWalletConvert INSTANCE = Mappers.getMapper(PayWalletConvert.class);

    AppPayWalletRespVO convert(PayWalletDO bean);
}
