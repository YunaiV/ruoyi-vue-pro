package cn.iocoder.yudao.module.iot.dal.mysql.plugin;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginInstanceDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IotPluginInstanceMapper extends BaseMapperX<IotPluginInstanceDO> {

    default IotPluginInstanceDO selectByProcessId(String processId) {
        return selectOne(IotPluginInstanceDO::getProcessId, processId);
    }

    default List<IotPluginInstanceDO> selectListByHeartbeatTimeLt(LocalDateTime heartbeatTime) {
        return selectList(new LambdaQueryWrapper<IotPluginInstanceDO>()
                .lt(IotPluginInstanceDO::getHeartbeatTime, heartbeatTime));
    }

}