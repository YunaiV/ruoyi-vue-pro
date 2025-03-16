package cn.iocoder.yudao.module.iot.dal.mysql.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task.IotOtaUpgradeTaskPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeTaskDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * OTA 升级任务Mapper
 *
 * @author Shelly
 */
@Mapper
public interface IotOtaUpgradeTaskMapper extends BaseMapperX<IotOtaUpgradeTaskDO> {

    /**
     * 根据固件ID和任务名称查询升级任务列表。
     *
     * @param firmwareId 固件ID，用于筛选升级任务
     * @param name       任务名称，用于筛选升级任务
     * @return 符合条件的升级任务列表
     */
    default List<IotOtaUpgradeTaskDO> selectByFirmwareIdAndName(Long firmwareId, String name) {
        return selectList(new LambdaQueryWrapperX<IotOtaUpgradeTaskDO>()
                .eqIfPresent(IotOtaUpgradeTaskDO::getFirmwareId, firmwareId)
                .eqIfPresent(IotOtaUpgradeTaskDO::getName, name));
    }

    /**
     * 分页查询升级任务列表，支持根据固件ID和任务名称进行筛选。
     *
     * @param pageReqVO 分页查询请求对象，包含分页参数和筛选条件
     * @return 分页结果，包含符合条件的升级任务列表
     */
    default PageResult<IotOtaUpgradeTaskDO> selectUpgradeTaskPage(IotOtaUpgradeTaskPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<IotOtaUpgradeTaskDO>()
                .eqIfPresent(IotOtaUpgradeTaskDO::getFirmwareId, pageReqVO.getFirmwareId())
                .likeIfPresent(IotOtaUpgradeTaskDO::getName, pageReqVO.getName()));
    }

    /**
     * 根据任务状态查询升级任务列表
     * <p>
     * 该函数通过传入的任务状态，查询数据库中符合条件的升级任务列表。
     *
     * @param status 任务状态，用于筛选升级任务的状态值
     * @return 返回符合条件的升级任务列表，列表中的每个元素为 IotOtaUpgradeTaskDO 对象
     */
    default List<IotOtaUpgradeTaskDO> selectUpgradeTaskByState(Integer status) {
        return selectList(IotOtaUpgradeTaskDO::getStatus, status);
    }

}
