package cn.iocoder.yudao.module.iot.dal.mysql.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.record.IotOtaTaskRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskRecordDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mapper
public interface IotOtaTaskRecordMapper extends BaseMapperX<IotOtaTaskRecordDO> {

    default List<IotOtaTaskRecordDO> selectListByFirmwareIdAndTaskId(Long firmwareId, Long taskId) {
        return selectList(new LambdaQueryWrapperX<IotOtaTaskRecordDO>()
                .eqIfPresent(IotOtaTaskRecordDO::getFirmwareId, firmwareId)
                .eqIfPresent(IotOtaTaskRecordDO::getTaskId, taskId)
                .select(IotOtaTaskRecordDO::getDeviceId, IotOtaTaskRecordDO::getStatus));
    }

    default PageResult<IotOtaTaskRecordDO> selectPage(IotOtaTaskRecordPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<IotOtaTaskRecordDO>()
                .eqIfPresent(IotOtaTaskRecordDO::getTaskId, pageReqVO.getTaskId())
                .eqIfPresent(IotOtaTaskRecordDO::getStatus, pageReqVO.getStatus()));
    }

    default List<IotOtaTaskRecordDO> selectListByTaskIdAndStatus(Long taskId, Collection<Integer> statuses) {
        return selectList(new LambdaQueryWrapperX<IotOtaTaskRecordDO>()
               .eq(IotOtaTaskRecordDO::getTaskId, taskId)
               .in(IotOtaTaskRecordDO::getStatus, statuses));
    }

    default Long selectCountByTaskIdAndStatus(Long taskId, Collection<Integer> statuses) {
        return selectCount(new LambdaQueryWrapperX<IotOtaTaskRecordDO>()
                .eq(IotOtaTaskRecordDO::getTaskId, taskId)
                .in(IotOtaTaskRecordDO::getStatus, statuses));
    }

    default int updateByIdAndStatus(Long id, Integer status,
                                    IotOtaTaskRecordDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<IotOtaTaskRecordDO>()
                .eq(IotOtaTaskRecordDO::getId, id)
                .eq(IotOtaTaskRecordDO::getStatus, status));
    }

    default int updateByIdAndStatus(Long id, Collection<Integer> whereStatuses,
                                    IotOtaTaskRecordDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<IotOtaTaskRecordDO>()
                .eq(IotOtaTaskRecordDO::getId, id)
                .in(IotOtaTaskRecordDO::getStatus, whereStatuses));
    }

    default void updateListByIdAndStatus(Collection<Long> ids, Collection<Integer> whereStatuses,
                                         IotOtaTaskRecordDO updateObj) {
        update(updateObj, new LambdaUpdateWrapper<IotOtaTaskRecordDO>()
                .in(IotOtaTaskRecordDO::getId, ids)
                .in(IotOtaTaskRecordDO::getStatus, whereStatuses));
    }

    default List<IotOtaTaskRecordDO> selectListByDeviceIdAndStatus(Set<Long> deviceIds, Set<Integer> statuses) {
        return selectList(new LambdaQueryWrapperX<IotOtaTaskRecordDO>()
                .inIfPresent(IotOtaTaskRecordDO::getDeviceId, deviceIds)
                .inIfPresent(IotOtaTaskRecordDO::getStatus, statuses));
    }

    default List<IotOtaTaskRecordDO> selectListByDeviceIdAndStatus(Long deviceId, Set<Integer> statuses) {
        return selectList(new LambdaQueryWrapperX<IotOtaTaskRecordDO>()
                .eqIfPresent(IotOtaTaskRecordDO::getDeviceId, deviceId)
                .inIfPresent(IotOtaTaskRecordDO::getStatus, statuses));
    }

    default List<IotOtaTaskRecordDO> selectListByStatus(Integer status) {
        return selectList(IotOtaTaskRecordDO::getStatus, status);
    }

}
