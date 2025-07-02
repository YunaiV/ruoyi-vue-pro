package cn.iocoder.yudao.module.iot.dal.mysql.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.IotOtaTaskPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IotOtaTaskMapper extends BaseMapperX<IotOtaTaskDO> {

    default IotOtaTaskDO selectByFirmwareIdAndName(Long firmwareId, String name) {
        return selectOne(IotOtaTaskDO::getFirmwareId, firmwareId,
                IotOtaTaskDO::getName, name);
    }

    default PageResult<IotOtaTaskDO> selectUpgradeTaskPage(IotOtaTaskPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<IotOtaTaskDO>()
                .eqIfPresent(IotOtaTaskDO::getFirmwareId, pageReqVO.getFirmwareId())
                .likeIfPresent(IotOtaTaskDO::getName, pageReqVO.getName()));
    }

}
