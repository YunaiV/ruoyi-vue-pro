package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
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
                .eqIfPresent(IotDeviceDO::getState, reqVO.getStatus())
                .apply(ObjectUtil.isNotNull(reqVO.getGroupId()), "FIND_IN_SET(" + reqVO.getGroupId() + ",group_ids) > 0")
                .orderByDesc(IotDeviceDO::getId));
    }

    default IotDeviceDO selectByDeviceName(String deviceName) {
        return selectOne(IotDeviceDO::getDeviceName, deviceName);
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
        return selectOne(new LambdaQueryWrapper<IotDeviceDO>()
                .apply("LOWER(device_key) = {0}", deviceKey.toLowerCase()));
    }

    default List<IotDeviceDO> selectListByDeviceType(Integer deviceType) {
        return selectList(IotDeviceDO::getDeviceType, deviceType);
    }

    default List<IotDeviceDO> selectListByState(Integer state) {
        return selectList(IotDeviceDO::getState, state);
    }

    default Long selectCountByGroupId(Long groupId) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceDO>()
                .apply("FIND_IN_SET(" + groupId + ",group_ids) > 0")
                .orderByDesc(IotDeviceDO::getId));
    }

    /**
     * 统计设备数量
     *
     * @param createTime 创建时间，如果为空，则统计所有设备数量
     * @return 设备数量
     */
    default Long selectCountByCreateTime(LocalDateTime createTime) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceDO>()
                .geIfPresent(IotDeviceDO::getCreateTime, createTime));
    }

    /**
     * 统计指定状态的设备数量
     *
     * @param state 状态
     * @return 设备数量
     */
    default Long selectCountByState(Integer state) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceDO>()
                .eqIfPresent(IotDeviceDO::getState, state));
    }

}