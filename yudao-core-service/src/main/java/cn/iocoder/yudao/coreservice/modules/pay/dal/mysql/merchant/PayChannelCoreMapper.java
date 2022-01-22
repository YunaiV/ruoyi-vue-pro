package cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.merchant;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface PayChannelCoreMapper extends BaseMapperX<PayChannelDO> {

    default PayChannelDO selectByAppIdAndCode(Long appId, String code) {
        return selectOne(PayChannelDO::getAppId, appId, PayChannelDO::getCode, code);
    }

    @Select("SELECT id FROM pay_channel WHERE update_time > #{maxUpdateTime} LIMIT 1")
    Long selectExistsByUpdateTimeAfter(Date maxUpdateTime);

}
