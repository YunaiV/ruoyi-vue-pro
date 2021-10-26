package cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.notify;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.notify.PayNotifyTaskDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayNotifyTaskCoreMapper extends BaseMapperX<PayNotifyTaskDO> {
}
