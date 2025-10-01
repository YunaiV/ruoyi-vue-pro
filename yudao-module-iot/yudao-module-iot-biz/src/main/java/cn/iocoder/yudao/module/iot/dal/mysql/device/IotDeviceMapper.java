package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Nullable;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    default List<IotDeviceDO> selectListByCondition(@Nullable Integer deviceType,
                                                    @Nullable Long productId) {
        return selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eqIfPresent(IotDeviceDO::getDeviceType, deviceType)
                .eqIfPresent(IotDeviceDO::getProductId, productId));
    }

    default List<IotDeviceDO> selectListByState(Integer state) {
        return selectList(IotDeviceDO::getState, state);
    }

    default List<IotDeviceDO> selectListByProductId(Long productId) {
        return selectList(IotDeviceDO::getProductId, productId);
    }

    default Long selectCountByGroupId(Long groupId) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceDO>()
                .apply("FIND_IN_SET(" + groupId + ",group_ids) > 0"));
    }

    default Long selectCountByCreateTime(@Nullable LocalDateTime createTime) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceDO>()
                .geIfPresent(IotDeviceDO::getCreateTime, createTime));
    }

    default List<IotDeviceDO> selectByProductKeyAndDeviceNames(String productKey, Collection<String> deviceNames) {
        return selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eq(IotDeviceDO::getProductKey, productKey)
                .in(IotDeviceDO::getDeviceName, deviceNames));
    }

    default IotDeviceDO selectBySerialNumber(String serialNumber) {
        return selectOne(IotDeviceDO::getSerialNumber, serialNumber);
    }

    /**
     * 查询指定产品下的设备数量
     *
     * @return 产品编号 -> 设备数量的映射
     */
    default Map<Long, Integer> selectDeviceCountMapByProductId() {
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<IotDeviceDO>()
                .select("product_id AS productId", "COUNT(1) AS deviceCount")
                .groupBy("product_id"));
        return result.stream().collect(Collectors.toMap(
            map -> Long.valueOf(map.get("productId").toString()),
            map -> Integer.valueOf(map.get("deviceCount").toString())
        ));
    }

    /**
     * 查询各个状态下的设备数量
     *
     * @return 设备状态 -> 设备数量的映射
     */
    default Map<Integer, Long> selectDeviceCountGroupByState() {
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<IotDeviceDO>()
                .select("state", "COUNT(1) AS deviceCount")
                .groupBy("state"));
        return result.stream().collect(Collectors.toMap(
            map -> Integer.valueOf(map.get("state").toString()),
            map -> Long.valueOf(map.get("deviceCount").toString())
        ));
    }

}
