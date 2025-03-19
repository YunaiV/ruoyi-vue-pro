package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 设备 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotDeviceMapper extends BaseMapperX<IotDeviceDO> {

    default PageResult<IotDeviceDO> selectPage(IotDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceDO>()
                .eqIfPresent(IotDeviceDO::getDeviceKey, reqVO.getDeviceKey())
                .likeIfPresent(IotDeviceDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(IotDeviceDO::getProductId, reqVO.getProductId())
                .eqIfPresent(IotDeviceDO::getProductKey, reqVO.getProductKey())
                .eqIfPresent(IotDeviceDO::getDeviceType, reqVO.getDeviceType())
                .likeIfPresent(IotDeviceDO::getNickname, reqVO.getNickname())
                .eqIfPresent(IotDeviceDO::getGatewayId, reqVO.getGatewayId())
                .eqIfPresent(IotDeviceDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotDeviceDO::getStatusLastUpdateTime, reqVO.getStatusLastUpdateTime())
                .betweenIfPresent(IotDeviceDO::getLastOnlineTime, reqVO.getLastOnlineTime())
                .betweenIfPresent(IotDeviceDO::getLastOfflineTime, reqVO.getLastOfflineTime())
                .betweenIfPresent(IotDeviceDO::getActiveTime, reqVO.getActiveTime())
                .eqIfPresent(IotDeviceDO::getDeviceSecret, reqVO.getDeviceSecret())
                .eqIfPresent(IotDeviceDO::getMqttClientId, reqVO.getMqttClientId())
                .likeIfPresent(IotDeviceDO::getMqttUsername, reqVO.getMqttUsername())
                .eqIfPresent(IotDeviceDO::getMqttPassword, reqVO.getMqttPassword())
                .eqIfPresent(IotDeviceDO::getAuthType, reqVO.getAuthType())
                .betweenIfPresent(IotDeviceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotDeviceDO::getId));
    }

    default IotDeviceDO selectByProductKeyAndDeviceName(String productKey, String deviceName) {
        return selectOne(IotDeviceDO::getProductKey, productKey,
                IotDeviceDO::getDeviceName, deviceName);
    }

    default long selectCountByGatewayId(Long id) {
        return selectCount(IotDeviceDO::getGatewayId, id);
    }

    default Long selectCountByProductId(Long productId) {
        return selectCount(IotDeviceDO::getProductId, productId);
    }
}