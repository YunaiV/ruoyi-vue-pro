package cn.iocoder.yudao.module.iot.service.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.record.IotOtaTaskRecordPageReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskRecordDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * IoT OTA 升级记录 Service 接口
 */
public interface IotOtaTaskRecordService {

    /**
     * 批量创建 OTA 升级记录
     *
     * @param devices      设备列表
     * @param firmwareId   固件编号
     * @param taskId       任务编号
     */
    void createOtaTaskRecordList(List<IotDeviceDO> devices, Long firmwareId, Long taskId);

    /**
     * 获取 OTA 升级记录的状态统计
     *
     * @param firmwareId 固件编号
     * @param taskId     任务编号
     * @return 状态统计 Map，key 为状态码，value 为对应状态的升级记录数量
     */
    Map<Integer, Long> getOtaTaskRecordStatusStatistics(Long firmwareId, Long taskId);

    /**
     * 获取 OTA 升级记录
     *
     * @param id 编号
     * @return OTA 升级记录
     */
    IotOtaTaskRecordDO getOtaTaskRecord(Long id);

    /**
     * 获取 OTA 升级记录分页
     *
     * @param pageReqVO 分页查询
     * @return OTA 升级记录分页
     */
    PageResult<IotOtaTaskRecordDO> getOtaTaskRecordPage(@Valid IotOtaTaskRecordPageReqVO pageReqVO);

    /**
     * 根据 OTA 任务编号，取消未结束的升级记录
     *
     * @param taskId 升级任务编号
     */
    void cancelTaskRecordListByTaskId(Long taskId);

    /**
     * 根据设备编号和记录状态，获取 OTA 升级记录列表
     *
     * @param deviceIds 设备编号集合
     * @param statuses  记录状态集合
     * @return OTA 升级记录列表
     */
    List<IotOtaTaskRecordDO> getOtaTaskRecordListByDeviceIdAndStatus(Set<Long> deviceIds, Set<Integer> statuses);

    /**
     * 根据记录状态，获取 OTA 升级记录列表
     *
     * @param status 升级记录状态
     * @return 升级记录列表
     */
    List<IotOtaTaskRecordDO> getOtaRecordListByStatus(Integer status);

    /**
     * 取消 OTA 升级记录
     *
     * @param id 记录编号
     */
    void cancelOtaTaskRecord(Long id);

    /**
     * 推送 OTA 升级任务记录
     *
     * @param record   任务记录
     * @param fireware 固件信息
     * @param device   设备信息
     * @return 是否推送成功
     */
    boolean pushOtaTaskRecord(IotOtaTaskRecordDO record, IotOtaFirmwareDO fireware, IotDeviceDO device);

    /**
     * 更新 OTA 升级记录进度
     *
     * @param device   设备信息
     * @param message  设备消息
     */
    void updateOtaRecordProgress(IotDeviceDO device, IotDeviceMessage message);

}
