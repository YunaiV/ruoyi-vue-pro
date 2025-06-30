package cn.iocoder.yudao.module.iot.service.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.record.IotOtaUpgradeRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskRecordDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

// TODO @li：注释写的有点冗余，可以看看别的模块哈。= = AI 生成的注释，有的时候太啰嗦了，需要处理下的哈
/**
 * IotOtaUpgradeRecordService 接口定义了与物联网设备OTA升级记录相关的操作。
 * 该接口提供了创建、更新、查询、统计和重试升级记录的功能。
 */
public interface IotOtaUpgradeRecordService {

    /**
     * 批量创建 OTA 升级记录
     * 该函数用于为指定的设备列表、固件ID和升级任务ID创建OTA升级记录。
     *
     * @param deviceIds     设备ID列表，表示需要升级的设备集合。
     * @param firmwareId    固件ID，表示要升级到的固件版本。
     * @param upgradeTaskId 升级任务ID，表示此次升级任务的唯一标识。
     */
    void createOtaUpgradeRecordBatch(List<Long> deviceIds, Long firmwareId, Long upgradeTaskId);

    /**
     * 获取 OTA 升级记录的数量统计
     *
     * @return 返回一个 Map，其中键为状态码，值为对应状态的升级记录数量
     */
    Map<Integer, Long> getOtaUpgradeRecordCount(@Valid IotOtaUpgradeRecordPageReqVO pageReqVO);

    /**
     * 获取 OTA 升级记录的统计信息。
     *
     * @return 返回一个 Map，其中键为状态码，值为对应状态的升级记录统计信息
     */
    Map<Integer, Long> getOtaUpgradeRecordStatistics(Long firmwareId);

    /**
     * 获取指定 ID 的 OTA 升级记录的详细信息。
     *
     * @param id 需要查询的升级记录的ID。
     * @return 返回包含升级记录详细信息的响应对象。
     */
    IotOtaTaskRecordDO getUpgradeRecord(Long id);

    /**
     * 分页查询 OTA 升级记录。
     *
     * @param pageReqVO 包含分页查询条件的请求对象，必须经过验证。
     * @return 返回包含分页查询结果的响应对象。
     */
    PageResult<IotOtaTaskRecordDO> getUpgradeRecordPage(@Valid IotOtaUpgradeRecordPageReqVO pageReqVO);

    /**
     * 根据任务 ID 取消升级记录
     * <p>
     * 该函数用于根据给定的任务ID，取消与该任务相关的升级记录。通常用于在任务执行失败或用户手动取消时，
     * 清理或标记相关的升级记录为取消状态。
     *
     * @param taskId 要取消升级记录的任务ID。该ID唯一标识一个任务，通常由任务管理系统生成。
     */
    void cancelUpgradeRecordByTaskId(Long taskId);

}
