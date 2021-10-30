package cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.merchant;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayAppCoreMapper extends BaseMapperX<PayAppDO> {
}
