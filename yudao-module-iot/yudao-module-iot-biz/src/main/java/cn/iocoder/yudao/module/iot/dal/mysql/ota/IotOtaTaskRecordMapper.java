package cn.iocoder.yudao.module.iot.dal.mysql.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.record.IotOtaTaskRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskRecordDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

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

    default void updateByTaskIdAndStatus(Long taskId, Integer fromStatus, IotOtaTaskRecordDO updateRecord) {
        update(updateRecord, new LambdaUpdateWrapper<IotOtaTaskRecordDO>()
                .eq(IotOtaTaskRecordDO::getTaskId, taskId)
                .eq(IotOtaTaskRecordDO::getStatus, fromStatus));
    }

    default List<IotOtaTaskRecordDO> selectListByDeviceIdAndStatus(Set<Long> deviceIds, Set<Integer> statuses) {
        return selectList(new LambdaQueryWrapperX<IotOtaTaskRecordDO>()
                .inIfPresent(IotOtaTaskRecordDO::getDeviceId, deviceIds)
                .inIfPresent(IotOtaTaskRecordDO::getStatus, statuses));
    }

}
