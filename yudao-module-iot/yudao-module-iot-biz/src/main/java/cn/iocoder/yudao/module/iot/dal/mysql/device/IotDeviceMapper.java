package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 设备 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotDeviceMapper extends BaseMapperX<IotDeviceDO> {

    default PageResult<IotDeviceDO> selectPage(IotDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceDO>()
                .likeIfPresent(IotDeviceDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(IotDeviceDO::getProductId, reqVO.getProductId())
                .eqIfPresent(IotDeviceDO::getDeviceType, reqVO.getDeviceType())
                .likeIfPresent(IotDeviceDO::getNickname, reqVO.getNickname())
                .eqIfPresent(IotDeviceDO::getStatus, reqVO.getStatus())
                .apply(ObjectUtil.isNotNull(reqVO.getGroupId()), "FIND_IN_SET(" + reqVO.getGroupId() + ",group_ids) > 0")
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

    default IotDeviceDO selectByDeviceKey(String deviceKey) {
        return selectOne(IotDeviceDO::getDeviceKey, deviceKey);
    }

    default List<IotDeviceDO> selectList(Integer deviceType) {
        return selectList(IotDeviceDO::getDeviceType, deviceType);
    }

}