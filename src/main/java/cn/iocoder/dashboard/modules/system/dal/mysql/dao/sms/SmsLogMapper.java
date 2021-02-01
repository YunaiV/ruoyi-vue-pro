package cn.iocoder.dashboard.modules.system.dal.mysql.dao.sms;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms.SmsLogDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SmsLogMapper extends BaseMapper<SmsLogDO> {

}
