package cn.iocoder.yudao.module.iot.dal.mysql.plugin;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginInstanceDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IotPluginInstanceMapper extends BaseMapperX<IotPluginInstanceDO> {

    // TODO @芋艿：方法名，重构
    default IotPluginInstanceDO selectByMainIdAndPluginId(String mainId, Long pluginId) {
        return selectOne(new LambdaQueryWrapperX<IotPluginInstanceDO>()
                .eq(IotPluginInstanceDO::getProcessId, mainId)
                .eq(IotPluginInstanceDO::getPluginId, pluginId));
    }

}