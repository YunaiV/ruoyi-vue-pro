package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusPointDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

    /**
     * 保存（更新）点位：bitIndex 非空时随实体写入；为空时显式置为 NULL，用于清空位索引
     * <p>
     * 默认更新策略会跳过 null 字段，所以只在为空时追加 set，保证 SET 中 bit_index 只出现一次；
     * 物模型改名的 {@link #updateByThingModelId} 仍跳过 null，不会误清位索引
     */
    default void updateForSave(IotDeviceModbusPointDO updateObj) {
        update(updateObj, new LambdaUpdateWrapper<IotDeviceModbusPointDO>()
                .eq(IotDeviceModbusPointDO::getId, updateObj.getId())
                // DONE @AI："jdbcType=INTEGER" 放到父类里，？我记得有个类似的；（父类 BaseMapperX 已有 JDBC_TYPE_VARCHAR，bit_index 是整数列不能复用，已在旁边补 JDBC_TYPE_INTEGER）
                .set(updateObj.getBitIndex() == null, IotDeviceModbusPointDO::getBitIndex, null, JDBC_TYPE_INTEGER));
    }

}
