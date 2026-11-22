package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaPointPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaPointDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * IoT 设备 OPC UA 点位配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotDeviceOpcUaPointMapper extends BaseMapperX<IotDeviceOpcUaPointDO> {

    default PageResult<IotDeviceOpcUaPointDO> selectPage(IotDeviceOpcUaPointPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceOpcUaPointDO>()
                .eqIfPresent(IotDeviceOpcUaPointDO::getDeviceId, reqVO.getDeviceId())
                .likeIfPresent(IotDeviceOpcUaPointDO::getNodeId, reqVO.getNodeId())
                .eqIfPresent(IotDeviceOpcUaPointDO::getCollectMode, reqVO.getCollectMode())
                .eqIfPresent(IotDeviceOpcUaPointDO::getStatus, reqVO.getStatus())
                .orderByDesc(IotDeviceOpcUaPointDO::getId));
    }

    default List<IotDeviceOpcUaPointDO> selectListByDeviceIdsAndStatus(Collection<Long> deviceIds, Integer status) {
        return selectList(new LambdaQueryWrapperX<IotDeviceOpcUaPointDO>()
                .in(IotDeviceOpcUaPointDO::getDeviceId, deviceIds)
                .eq(IotDeviceOpcUaPointDO::getStatus, status)
                .orderByAsc(IotDeviceOpcUaPointDO::getId));
    }

    default IotDeviceOpcUaPointDO selectByDeviceIdAndThingModelId(Long deviceId, Long thingModelId) {
        return selectFirstOne(new LambdaQueryWrapperX<IotDeviceOpcUaPointDO>()
                .eq(IotDeviceOpcUaPointDO::getDeviceId, deviceId)
                .eq(IotDeviceOpcUaPointDO::getThingModelId, thingModelId)
                .orderByAsc(IotDeviceOpcUaPointDO::getId));
    }

}
