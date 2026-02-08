package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusPointDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * IoT 设备 Modbus 点位配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotDeviceModbusPointMapper extends BaseMapperX<IotDeviceModbusPointDO> {

    default PageResult<IotDeviceModbusPointDO> selectPage(IotDeviceModbusPointPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceModbusPointDO>()
                .eqIfPresent(IotDeviceModbusPointDO::getDeviceId, reqVO.getDeviceId())
                .likeIfPresent(IotDeviceModbusPointDO::getIdentifier, reqVO.getIdentifier())
                .likeIfPresent(IotDeviceModbusPointDO::getName, reqVO.getName())
                .eqIfPresent(IotDeviceModbusPointDO::getFunctionCode, reqVO.getFunctionCode())
                .eqIfPresent(IotDeviceModbusPointDO::getStatus, reqVO.getStatus())
                .orderByDesc(IotDeviceModbusPointDO::getId));
    }

    default List<IotDeviceModbusPointDO> selectListByDeviceIdsAndStatus(Collection<Long> deviceIds, Integer status) {
        return selectList(new LambdaQueryWrapperX<IotDeviceModbusPointDO>()
                .in(IotDeviceModbusPointDO::getDeviceId, deviceIds)
                .eq(IotDeviceModbusPointDO::getStatus, status));
    }

    default IotDeviceModbusPointDO selectByDeviceIdAndIdentifier(Long deviceId, String identifier) {
        return selectOne(IotDeviceModbusPointDO::getDeviceId, deviceId,
                IotDeviceModbusPointDO::getIdentifier, identifier);
    }

    default void updateByThingModelId(Long thingModelId, IotDeviceModbusPointDO updateObj) {
        update(updateObj, new LambdaQueryWrapperX<IotDeviceModbusPointDO>()
                .eq(IotDeviceModbusPointDO::getThingModelId, thingModelId));
    }

}
