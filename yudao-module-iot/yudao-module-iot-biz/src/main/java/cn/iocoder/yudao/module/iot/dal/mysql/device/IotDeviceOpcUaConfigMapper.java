package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigListReqDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 设备 OPC UA 连接配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotDeviceOpcUaConfigMapper extends BaseMapperX<IotDeviceOpcUaConfigDO> {

    default IotDeviceOpcUaConfigDO selectByDeviceId(Long deviceId) {
        return selectFirstOne(new LambdaQueryWrapperX<IotDeviceOpcUaConfigDO>()
                .eq(IotDeviceOpcUaConfigDO::getDeviceId, deviceId)
                .orderByAsc(IotDeviceOpcUaConfigDO::getId));
    }

    default List<IotDeviceOpcUaConfigDO> selectList(IotOpcUaDeviceConfigListReqDTO reqDTO) {
        return selectList(new LambdaQueryWrapperX<IotDeviceOpcUaConfigDO>()
                .eqIfPresent(IotDeviceOpcUaConfigDO::getStatus, reqDTO.getStatus())
                .inIfPresent(IotDeviceOpcUaConfigDO::getDeviceId, reqDTO.getDeviceIds())
                .orderByAsc(IotDeviceOpcUaConfigDO::getId));
    }

}
