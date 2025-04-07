package cn.iocoder.yudao.module.iot.dal.mysql.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.record.IotOtaUpgradeRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeRecordDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface IotOtaUpgradeRecordMapper extends BaseMapperX<IotOtaUpgradeRecordDO> {

    // TODO @li：selectByFirmwareIdAndTaskIdAndDeviceId；让方法自解释
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

    // TODO @li：这个是不是 groupby status 就 ok 拉？
    /**
     * 根据任务ID和设备名称查询OTA升级记录的状态统计信息。
     * 该函数通过SQL查询统计不同状态（0到5）的记录数量，并返回一个包含统计结果的Map列表。
     *
     * @param taskId     任务ID，用于筛选特定任务的OTA升级记录。
     * @param deviceName 设备名称，支持模糊查询，用于筛选特定设备的OTA升级记录。
     * @return 返回一个Map列表，每个Map包含不同状态（0到5）的记录数量。
     */
    @Select("select count(case when status = 0 then 1 else 0) as `0` " +
            "count(case when status = 1 then 1 else 0) as `1` " +
            "count(case when status = 2 then 1 else 0) as `2` " +
            "count(case when status = 3 then 1 else 0) as `3` " +
            "count(case when status = 4 then 1 else 0) as `4` " +
            "count(case when status = 5 then 1 else 0) as `5` " +
            "from iot_ota_upgrade_record " +
            "where task_id = #{taskId} " +
            "and device_name like concat('%', #{deviceName}, '%') " +
            "and status = #{status}")
    List<Map<String, Object>> selectOtaUpgradeRecordCount(@Param("taskId") Long taskId,
                                                          @Param("deviceName") String deviceName);

    /**
     * 根据固件ID查询OTA升级记录的状态统计信息。
     * 该函数通过SQL查询统计不同状态（0到5）的记录数量，并返回一个包含统计结果的Map列表。
     *
     * @param firmwareId 固件ID，用于筛选特定固件的OTA升级记录。
     * @return 返回一个Map列表，每个Map包含不同状态（0到5）的记录数量。
     */
    @Select("select count(case when status = 0 then 1 else 0) as `0` " +
            "count(case when status = 1 then 1 else 0) as `1` " +
            "count(case when status = 2 then 1 else 0) as `2` " +
            "count(case when status = 3 then 1 else 0) as `3` " +
            "count(case when status = 4 then 1 else 0) as `4` " +
            "count(case when status = 5 then 1 else 0) as `5` " +
            "from iot_ota_upgrade_record " +
            "where firmware_id = #{firmwareId}")
    List<Map<String, Object>> selectOtaUpgradeRecordStatistics(Long firmwareId);

    // TODO @li：这里的注释，可以去掉哈
    /**
     * 根据分页查询条件获取 OTA升级记录的分页结果
     *
     * @param pageReqVO 分页查询请求参数，包含设备名称、任务ID等查询条件
     * @return 返回分页查询结果，包含符合条件的 OTA升级记录列表
     */
    // TODO @li：selectPage 就 ok 拉。
    default PageResult<IotOtaUpgradeRecordDO> selectUpgradeRecordPage(IotOtaUpgradeRecordPageReqVO pageReqVO) {
        // TODO @li：这里的注释，可以去掉哈；然后下面的“如果”。。。也没必要注释
        // 使用LambdaQueryWrapperX构建查询条件，并根据请求参数动态添加查询条件
        return selectPage(pageReqVO, new LambdaQueryWrapperX<IotOtaUpgradeRecordDO>()
                .likeIfPresent(IotOtaUpgradeRecordDO::getDeviceName, pageReqVO.getDeviceName()) // 如果设备名称存在，则添加模糊查询条件
                .eqIfPresent(IotOtaUpgradeRecordDO::getTaskId, pageReqVO.getTaskId())); // 如果任务ID存在，则添加等值查询条件
    }

    // TODO @li：这里的注释，可以去掉哈
    /**
     * 根据任务ID和状态更新升级记录的状态
     * <p>
     * 该函数用于将符合指定任务ID和状态的升级记录的状态更新为新的状态。
     *
     * @param setStatus   要设置的新状态值，类型为Integer
     * @param taskId      要更新的升级记录对应的任务ID，类型为Long
     * @param whereStatus 用于筛选升级记录的当前状态值，类型为Integer
     */
    // TODO @li：改成 updateByTaskIdAndStatus(taskId, status, IotOtaUpgradeRecordDO) 更通用一些。
    default void updateUpgradeRecordStatusByTaskIdAndStatus(Integer setStatus, Long taskId, Integer whereStatus) {
        // 使用LambdaUpdateWrapper构建更新条件，将指定状态的记录更新为指定状态
        update(new LambdaUpdateWrapper<IotOtaUpgradeRecordDO>()
                .set(IotOtaUpgradeRecordDO::getStatus, setStatus)
                .eq(IotOtaUpgradeRecordDO::getTaskId, taskId)
                .eq(IotOtaUpgradeRecordDO::getStatus, whereStatus)
        );
    }

    // TODO @li：参考上面的建议，调整下这个方法
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

    // TODO @li：参考上面的建议，调整下这个方法
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

    // TODO @li：参考上面的建议，调整下这个方法
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
