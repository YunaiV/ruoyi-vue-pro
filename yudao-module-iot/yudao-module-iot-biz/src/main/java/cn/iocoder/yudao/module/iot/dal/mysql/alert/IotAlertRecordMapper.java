package cn.iocoder.yudao.module.iot.dal.mysql.alert;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.recrod.IotAlertRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertRecordDO;
import org.apache.ibatis.annotations.Mapper;

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

}