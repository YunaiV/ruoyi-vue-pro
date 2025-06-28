package cn.iocoder.yudao.module.iot.dal.mysql.alert;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.recrod.IotAlertRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertRecordDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * IoT 告警记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAlertRecordMapper extends BaseMapperX<IotAlertRecordDO> {

    default PageResult<IotAlertRecordDO> selectPage(IotAlertRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotAlertRecordDO>()
                .eqIfPresent(IotAlertRecordDO::getConfigId, reqVO.getConfigId())
                .eqIfPresent(IotAlertRecordDO::getConfigLevel, reqVO.getLevel())
                .eqIfPresent(IotAlertRecordDO::getProductId, reqVO.getProductId())
                .eqIfPresent(IotAlertRecordDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IotAlertRecordDO::getProcessStatus, reqVO.getProcessStatus())
                .betweenIfPresent(IotAlertRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotAlertRecordDO::getId));
    }

    default List<IotAlertRecordDO> selectListBySceneRuleId(Long sceneRuleId, Long deviceId, Boolean processStatus) {
        return selectList(new LambdaQueryWrapperX<IotAlertRecordDO>()
                .eq(IotAlertRecordDO::getSceneRuleId, sceneRuleId)
                .eqIfPresent(IotAlertRecordDO::getDeviceId, deviceId)
                .eqIfPresent(IotAlertRecordDO::getProcessStatus, processStatus)
                .orderByDesc(IotAlertRecordDO::getId));
    }

    default int updateList(Collection<Long> ids, IotAlertRecordDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<IotAlertRecordDO>()
                .in(IotAlertRecordDO::getId, ids));
    }

}