package cn.iocoder.yudao.module.iot.dal.mysql.opcuaserver;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerNodePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerNodeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * IoT OPC UA Server 节点 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotOpcUaServerNodeMapper extends BaseMapperX<IotOpcUaServerNodeDO> {

    default PageResult<IotOpcUaServerNodeDO> selectPage(IotOpcUaServerNodePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotOpcUaServerNodeDO>()
                .eq(IotOpcUaServerNodeDO::getConfigId, reqVO.getConfigId())
                .eqIfPresent(IotOpcUaServerNodeDO::getDeviceId, reqVO.getDeviceId())
                .likeIfPresent(IotOpcUaServerNodeDO::getBrowseName, reqVO.getBrowseName())
                .eqIfPresent(IotOpcUaServerNodeDO::getStatus, reqVO.getStatus())
                .orderByDesc(IotOpcUaServerNodeDO::getId));
    }

    default IotOpcUaServerNodeDO selectByConfigIdAndBrowseName(Long configId, String browseName) {
        return selectFirstOne(new LambdaQueryWrapperX<IotOpcUaServerNodeDO>()
                .eq(IotOpcUaServerNodeDO::getConfigId, configId)
                .eq(IotOpcUaServerNodeDO::getBrowseName, browseName)
                .orderByAsc(IotOpcUaServerNodeDO::getId));
    }

    default IotOpcUaServerNodeDO selectByConfigIdAndDeviceIdAndThingModelId(Long configId, Long deviceId, Long thingModelId) {
        return selectFirstOne(new LambdaQueryWrapperX<IotOpcUaServerNodeDO>()
                .eq(IotOpcUaServerNodeDO::getConfigId, configId)
                .eq(IotOpcUaServerNodeDO::getDeviceId, deviceId)
                .eq(IotOpcUaServerNodeDO::getThingModelId, thingModelId)
                .orderByAsc(IotOpcUaServerNodeDO::getId));
    }

    default List<IotOpcUaServerNodeDO> selectListByConfigIdsAndStatus(Collection<Long> configIds, Integer status) {
        return selectList(new LambdaQueryWrapperX<IotOpcUaServerNodeDO>()
                .in(IotOpcUaServerNodeDO::getConfigId, configIds)
                .eq(IotOpcUaServerNodeDO::getStatus, status)
                .orderByAsc(IotOpcUaServerNodeDO::getId));
    }

    default Long selectCountByConfigId(Long configId) {
        return selectCount(IotOpcUaServerNodeDO::getConfigId, configId);
    }

}
