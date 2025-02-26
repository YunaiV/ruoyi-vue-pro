package cn.iocoder.yudao.module.iot.dal.mysql.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.record.IotOtaUpgradeRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeRecordDO;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeRecordStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// TODO @li：这里的注释，可以去掉哈，多了点点
/**
 * OTA 升级记录 Mapper 接口
 */
@Mapper
public interface IotOtaUpgradeRecordMapper extends BaseMapperX<IotOtaUpgradeRecordDO> {

    /**
     * 根据条件查询单个OTA升级记录
     *
     * @param firmwareId 固件ID，可选参数，用于筛选固件ID匹配的记录
     * @param taskId     任务ID，可选参数，用于筛选任务ID匹配的记录
     * @param deviceId   设备ID，可选参数，用于筛选设备ID匹配的记录
     * @return 返回符合条件的单个OTA升级记录，如果不存在则返回null
     */
    default IotOtaUpgradeRecordDO selectByConditions(Long firmwareId, Long taskId, String deviceId) {
        // 使用LambdaQueryWrapperX构建查询条件，根据传入的参数动态添加查询条件
        return selectOne(new LambdaQueryWrapperX<IotOtaUpgradeRecordDO>()
                .eqIfPresent(IotOtaUpgradeRecordDO::getFirmwareId, firmwareId)
                .eqIfPresent(IotOtaUpgradeRecordDO::getTaskId, taskId)
                .eqIfPresent(IotOtaUpgradeRecordDO::getDeviceId, deviceId));
    }

    /**
     * 获取OTA升级记录的数量
     *
     * @param taskId     任务ID，用于筛选特定任务的升级记录
     * @param deviceName 设备名称，用于筛选特定设备的升级记录
     * @param status     状态，用于筛选特定状态的升级记录
     * @return 返回符合条件的OTA升级记录的数量
     */
    Long getOtaUpgradeRecordCount(@Param("taskId") Long taskId,
                                  @Param("deviceName") String deviceName,
                                  @Param("status") Integer status);

    /**
     * 获取OTA升级记录的统计信息
     *
     * @param firmwareId 固件ID，用于筛选特定固件的升级记录
     * @param status     状态，用于筛选特定状态的升级记录
     * @return 返回符合条件的OTA升级记录的统计信息
     */
    Long getOtaUpgradeRecordStatistics(@Param("firmwareId") Long firmwareId,
                                       @Param("status") Integer status);


    /**
     * 根据分页查询条件获取IOT OTA升级记录的分页结果
     *
     * @param pageReqVO 分页查询请求参数，包含设备名称、任务ID等查询条件
     * @return 返回分页查询结果，包含符合条件的IOT OTA升级记录列表
     */
    default PageResult<IotOtaUpgradeRecordDO> selectUpgradeRecordPage(IotOtaUpgradeRecordPageReqVO pageReqVO) {
        // 使用LambdaQueryWrapperX构建查询条件，并根据请求参数动态添加查询条件
        return selectPage(pageReqVO, new LambdaQueryWrapperX<IotOtaUpgradeRecordDO>()
                .likeIfPresent(IotOtaUpgradeRecordDO::getDeviceName, pageReqVO.getDeviceName()) // 如果设备名称存在，则添加模糊查询条件
                .eqIfPresent(IotOtaUpgradeRecordDO::getTaskId, pageReqVO.getTaskId())); // 如果任务ID存在，则添加等值查询条件
    }

    /**
     * 根据任务ID取消升级记录。
     * 该方法通过任务ID查找状态为“待处理”的升级记录，并将其状态更新为“已取消”。
     *
     * @param taskId 任务ID，用于查找对应的升级记录。
     */
    default void cancelUpgradeRecordByTaskId(Long taskId) {
        // 使用LambdaUpdateWrapper构建更新条件，将状态为“待处理”的记录更新为“已取消”
        // TODO @li：哪些可以更新，通过 service 传递。mapper 尽量不要有逻辑
        update(new LambdaUpdateWrapper<IotOtaUpgradeRecordDO>()
                .set(IotOtaUpgradeRecordDO::getStatus, IotOtaUpgradeRecordStatusEnum.CANCELED.getStatus())
                .eq(IotOtaUpgradeRecordDO::getTaskId, taskId)
                .eq(IotOtaUpgradeRecordDO::getStatus, IotOtaUpgradeRecordStatusEnum.PENDING.getStatus())
        );
    }

    /**
     * 根据状态查询符合条件的升级记录列表
     * <p>
     * 该函数使用LambdaQueryWrapperX构建查询条件，查询指定状态的升级记录。
     *
     * @param state 升级记录的状态，用于筛选符合条件的记录
     * @return 返回符合指定状态的升级记录列表，类型为List<IotOtaUpgradeRecordDO>
     */
    default List<IotOtaUpgradeRecordDO> selectUpgradeRecordListByState(Integer state) {
        // 使用LambdaQueryWrapperX构建查询条件，根据状态查询符合条件的升级记录
        return selectList(new LambdaQueryWrapperX<IotOtaUpgradeRecordDO>()
                .eq(IotOtaUpgradeRecordDO::getStatus, state));
    }

    /**
     * 更新升级记录状态
     * <p>
     * 该函数用于批量更新指定ID列表中的升级记录状态。通过传入的ID列表和状态值，使用LambdaUpdateWrapper构建更新条件，
     * 并执行更新操作。
     *
     * @param ids    需要更新的升级记录ID列表，类型为List<Long>。传入的ID列表中的记录将被更新。
     * @param status 要更新的状态值，类型为Integer。该值将被设置到符合条件的升级记录中。
     */
    default void updateUpgradeRecordStatus(List<Long> ids, Integer status) {
        // 使用LambdaUpdateWrapper构建更新条件，设置状态字段，并根据ID列表进行筛选
        update(new LambdaUpdateWrapper<IotOtaUpgradeRecordDO>()
                .set(IotOtaUpgradeRecordDO::getStatus, status)
                .in(IotOtaUpgradeRecordDO::getId, ids)
        );
    }

    /**
     * 根据任务ID查询升级记录列表
     * <p>
     * 该函数通过任务ID查询符合条件的升级记录，并返回查询结果列表。
     *
     * @param taskId 任务ID，用于筛选升级记录
     * @return 返回符合条件的升级记录列表，若未找到则返回空列表
     */
    default List<IotOtaUpgradeRecordDO> selectUpgradeRecordListByTaskId(Long taskId) {
        // 使用LambdaQueryWrapperX构建查询条件，根据任务ID查询符合条件的升级记录
        return selectList(new LambdaQueryWrapperX<IotOtaUpgradeRecordDO>()
                .eq(IotOtaUpgradeRecordDO::getTaskId, taskId));
    }

}
